package com.moji.controller;
import com.moji.FilePathEnum;
import com.moji.R;
import com.moji.mapper.VideosMapper;
import com.moji.po.Videos;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.VideosService;
import com.moji.util.RemoteVideoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private VideosService videosService;

    @Autowired
    private VideosMapper videosMapper;

    private final Map<String, Integer> uploadOwners = new ConcurrentHashMap<>();
    private final Map<String, Integer> mergedVideoOwners = new ConcurrentHashMap<>();


    /**
     * 上传视频
     * @param files
     * @param uid
     * @param videos
     * @return
     */
    @PostMapping("/uploadVideo")
    public R<String> uploadMedia(@RequestParam(value = "file",required = false) MultipartFile[] files,@RequestParam(value = "uId",required = false) Integer uid, @RequestParam(value = "videoName",required = false) String mergedVideoName, @ModelAttribute Videos videos,@RequestHeader("Authorization") String token) {

        if(FilePathEnum.canUpload()){
            return R.error("上传文件过多，请联系管理员处理");
        }

        String videoName=mergedVideoName;
        String coverName=null;
        //远程视频投稿不需要上传视频文件
        boolean remoteVideo=videos.getVideoSource()!=null&&videos.getVideoSource()==1;
        if(remoteVideo){
            videoName=null;
            String remoteUrl=RemoteVideoUtil.parseRemoteUrl(videos.getRemoteUrl());
            if(remoteUrl==null)
                return R.error("视频直链无效");
            videos.setRemoteUrl(remoteUrl);
        }
        if(uid!=null)
            if (uid == 0) {
                return R.success("");
            }

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(videos.getUserId(),token))
            return R.error("操作失败");

        if(videoName!=null&&!validMergedVideo(videoName,videos.getUserId())){
            return R.error("视频文件不存在");
        }

        if(files!=null)
        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null) {
                return R.error("文件类型未知");
            }

            try {
                // 创建上传目录
                File uploadDir;
                UUID fileName = UUID.randomUUID(); // 生成 UUID

                if (contentType.startsWith("video/")) {
                    uploadDir = new File(FilePathEnum.UPLOAD_VIDEO.getPath());

                } else if (contentType.startsWith("image/")) {
                    uploadDir = new File(FilePathEnum.UPLOAD_VIDEO_COVER.getPath());
                } else {
                    return R.error("只允许上传图片和视频文件");
                }

                // 创建目录（如果不存在），并发下mkdirs可能返回false，需二次确认
                if (!uploadDir.isDirectory() && (!uploadDir.mkdirs() && !uploadDir.isDirectory())) {
                    return R.error("创建上传目录失败");
                }

                // 获取原文件名和扩展名
                String originalFilename = file.getOriginalFilename();
                String extension = "";

                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                if (contentType.startsWith("video/"))
                {
                    videoName = fileName + extension; // 新的文件名保留扩展名
                    File dest = new File(uploadDir, videoName);
                    file.transferTo(dest);
                }
                if (contentType.startsWith("image/"))
                {
                    coverName = fileName + ".webp"; // 新的文件名保留扩展名
                    FilePathEnum.saveAsWebp(file.getBytes(),uploadDir, String.valueOf(fileName));
                }
            } catch (Exception e) {
                return R.error("文件上传失败: " );
            }
        }

        //远程视频投稿仍然必须有封面
        if(remoteVideo&&coverName==null)
            return R.error("请上传封面");

        Boolean b = videosService.insertVideo(videoName, coverName, videos);
        if (b) {
            //videoName可能为空，直接remove会因null key抛NPE
            if(videoName!=null)
                mergedVideoOwners.remove(videoName);
            return R.success("上传成功");
        }

        return R.error("上传失败");

    }

    /**
     * 上传视频分片
     */
    @PostMapping("/uploadVideoChunk")
    public R<Integer> uploadVideoChunk(@RequestParam("file") MultipartFile file,
                                       @RequestParam String uploadId,
                                       @RequestParam Integer chunkIndex,
                                       @RequestParam Integer totalChunks,
                                       @RequestParam Integer userId,
                                       @RequestHeader("Authorization") String token) {

        if(FilePathEnum.canUpload()){
            return R.error("上传文件过多，请联系管理员处理");
        }

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("操作失败");

        if(!validUploadId(uploadId)||chunkIndex==null||totalChunks==null||chunkIndex<0||chunkIndex>=totalChunks){
            return R.error("分片参数错误");
        }

        Integer owner = uploadOwners.putIfAbsent(uploadId, userId);
        if(owner!=null&&!owner.equals(userId)){
            return R.error("操作失败");
        }

        File chunkDir = ensureChunkDir(uploadId);
        if (chunkDir == null) {
            return R.error("创建分片目录失败");
        }

        File dest = new File(chunkDir, chunkIndex + ".part");
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            return R.error("分片上传失败");
        }

        return R.success(chunkIndex);
    }

    /**
     * 合并视频分片
     */
    @PostMapping("/mergeVideoChunks")
    public R<String> mergeVideoChunks(@RequestParam String uploadId,
                                      @RequestParam Integer totalChunks,
                                      @RequestParam String originalFilename,
                                      @RequestParam Integer userId,
                                      @RequestHeader("Authorization") String token) {

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("操作失败");

        if(!validUploadId(uploadId)||totalChunks==null||totalChunks<=0){
            return R.error("分片参数错误");
        }

        if(!uploadBelongsToUser(uploadId,userId)){
            return R.error("操作失败");
        }

        File chunkDir = new File(getChunkRoot(), uploadId);
        if (!chunkDir.exists()||!chunkDir.isDirectory()) {
            return R.error("分片不存在");
        }

        File uploadDir = new File(FilePathEnum.UPLOAD_VIDEO.getPath());
        if (!uploadDir.isDirectory() && (!uploadDir.mkdirs() && !uploadDir.isDirectory())) {
            return R.error("创建上传目录失败");
        }

        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String videoName = UUID.randomUUID() + extension;
        File dest = new File(uploadDir, videoName);

        try (OutputStream outputStream = new FileOutputStream(dest)) {
            for (int i = 0; i < totalChunks; i++) {
                Path chunkPath = Paths.get(chunkDir.getPath(), i + ".part");
                if (!Files.exists(chunkPath)) {
                    //分片不完整时删除半成品文件并清理残留分片，避免占用磁盘
                    Files.deleteIfExists(dest.toPath());
                    cleanChunksQuietly(uploadId, chunkDir);
                    return R.error("分片不完整");
                }
                Files.copy(chunkPath, outputStream);
            }
        } catch (IOException e) {
            //合并失败同样清理半成品与残留分片
            try {
                Files.deleteIfExists(dest.toPath());
            } catch (IOException ignore) {
            }
            cleanChunksQuietly(uploadId, chunkDir);
            return R.error("分片合并失败");
        }

        deleteDirectory(chunkDir);
        uploadOwners.remove(uploadId);
        mergedVideoOwners.put(videoName,userId);
        return R.success(videoName);
    }

    /**
     * 清理未提交的视频上传临时文件
     */
    @DeleteMapping("/cleanVideoUpload")
    public R<String> cleanVideoUpload(@RequestParam(value = "uploadId",required = false) String uploadId,
                                      @RequestParam(value = "videoName",required = false) String videoName,
                                      @RequestParam Integer userId,
                                      @RequestHeader("Authorization") String token) {

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("操作失败");

        if(uploadId!=null&&validUploadId(uploadId)&&uploadBelongsToUser(uploadId,userId)){
            deleteDirectory(new File(getChunkRoot(), uploadId));
            uploadOwners.remove(uploadId);
        }

        if(videoName!=null&&validMergedVideo(videoName,userId)){
            try {
                Files.deleteIfExists(Paths.get(FilePathEnum.UPLOAD_VIDEO.getPath(), videoName));
                mergedVideoOwners.remove(videoName);
            } catch (IOException e) {
                return R.error("清理视频失败");
            }
        }

        return R.success("清理成功");
    }


    /**
     * 编辑视频
     * @param files
     * @param uid
     * @param videos
     * @return
     */
    @Caching(evict = {
            @CacheEvict(value = "dynamic", allEntries = true),
            @CacheEvict(value = "collect",allEntries = true ),
            @CacheEvict(value = "videoTitle",key = "#videos.id")
    })
    @PutMapping("/updateVideo")
    public R<String> updateVideo(@RequestParam(value = "file",required = false) MultipartFile[] files,@RequestParam(value = "userId",required = false) Integer uid, @RequestParam(value = "videoName",required = false) String mergedVideoName, @ModelAttribute Videos videos,@RequestHeader("Authorization") String token) {

        String videoName = mergedVideoName;
        String coverName = null;
        boolean vFlag=mergedVideoName!=null&&!mergedVideoName.isBlank();
        boolean cFlag=false;
        if (uid == 0) {
            return R.success("你还没有提交参数");
        }

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        //videos由表单绑定，videos.getUserId()不可信，必须以库里视频的真实归属为准
        Videos dbVideo=videosMapper.selectById(videos.getId());
        if(dbVideo==null)
            return R.error("视频不存在");
        if(!limiterServer.checkUser(dbVideo.getUserId(),token))
            return R.error("操作失败");

        if(videoName!=null&&!validMergedVideo(videoName,dbVideo.getUserId())){
            return R.error("视频文件不存在");
        }

        if(vFlag){
            deleteOldVideoFile(dbVideo.getId());
        }

        if (files != null) {
            for (MultipartFile filed : files) {
                String contentType = filed.getContentType();
                if (contentType != null) {
                    if (contentType.startsWith("video/")) {
                        vFlag = true;
                        Videos video = videosMapper.selectById(videos.getId());
                        String videoAddress = video.getVideoAddress();
                        if (videoAddress != null) {
                            int indexOf1 = videoAddress.lastIndexOf('/');
                            String videoFile = (indexOf1 != -1) ? videoAddress.substring(indexOf1 + 1) : videoAddress;
                            Path videoPath = Paths.get(FilePathEnum.UPLOAD_VIDEO.getPath() + videoFile);
                            if (Files.exists(videoPath)) {
                                try {
                                    Files.delete(videoPath);
                                } catch (IOException e) {
                                }
                            } else {
                            }
                        } else {
                        }
                    }
                    if (contentType.startsWith("image/")) {
                        cFlag = true;
                        Videos video = videosMapper.selectById(videos.getId());
                        String coverAddress = video.getCoverAddress();
                        if (coverAddress != null) {
                            int indexOf = coverAddress.lastIndexOf('/');
                            String coverFile = (indexOf != -1) ? coverAddress.substring(indexOf + 1) : coverAddress;
                            Path coverPath = Paths.get(FilePathEnum.UPLOAD_VIDEO_COVER.getPath()+ coverFile);
                            if (Files.exists(coverPath)) {
                                try {
                                    Files.delete(coverPath);
                                } catch (IOException e) {
                                }
                            } else {
                            }
                        } else {
                        }
                    }
                }
            }
        }


        if (files != null) {

            for (MultipartFile file : files) {
                String contentType = file.getContentType();
                if (contentType == null) {
                    return R.error("文件类型未知");
                }

                try {
                    // 创建上传目录
                    File uploadDir;
                    UUID fileName = UUID.randomUUID(); // 生成 UUID

                    if (contentType.startsWith("video/")) {
                        uploadDir = new File(FilePathEnum.UPLOAD_VIDEO.getPath());
                    } else if (contentType.startsWith("image/")) {
                        uploadDir = new File(FilePathEnum.UPLOAD_VIDEO_COVER.getPath());
                    } else {
                        return R.error("只允许上传图片和视频文件");
                    }

                    // 创建目录（如果不存在），并发下mkdirs可能返回false，需二次确认
                    if (!uploadDir.isDirectory() && (!uploadDir.mkdirs() && !uploadDir.isDirectory())) {
                        return R.error("创建上传目录失败");
                    }

                    // 获取原文件名和扩展名
                    String originalFilename = file.getOriginalFilename();
                    String extension = "";

                    if (originalFilename != null && originalFilename.contains(".")) {
                        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    }
                    if (contentType.startsWith("video/")) {
                        videoName = fileName + extension; // 新的文件名保留扩展名
                        File dest = new File(uploadDir, videoName);
                        file.transferTo(dest);
                    }
                    if (contentType.startsWith("image/")) {
                        coverName = fileName + ".webp"; // 新的文件名保留扩展名
                        FilePathEnum.saveAsWebp(file.getBytes(), uploadDir, String.valueOf(fileName));
                    }


                } catch (Exception e) {
                    return R.error("文件上传失败: ");
                }
            }
        }
        Boolean flag = videosService.updateVideo(videoName, coverName, videos,vFlag,cFlag);
        if (flag) {
            //仅修改封面等场景不传videoName，此时key为null会导致ConcurrentHashMap抛NPE
            if(videoName!=null)
                mergedVideoOwners.remove(videoName);
            return R.success("修改成功");
        }

        return R.error("修改失败");

    }

    private boolean validUploadId(String uploadId){
        return uploadId!=null&&uploadId.matches("[A-Za-z0-9_-]{1,80}");
    }

    /**
     * 分片根目录，使用File构造器拼接，避免依赖路径末尾是否带分隔符
     */
    private File getChunkRoot(){
        return new File(FilePathEnum.UPLOAD_VIDEO.getPath(),"chunks");
    }

    /**
     * 分片根目录，供定时清理任务使用
     */
    public File getChunkRootForClean(){
        return getChunkRoot();
    }

    /**
     * 删除指定分片目录及其上传者记录，供定时清理任务使用
     */
    public void deleteChunkDirForClean(File chunkDir){
        if(chunkDir==null)
            return;
        String uploadId=chunkDir.getName();
        deleteDirectory(chunkDir);
        if(uploadId!=null)
            uploadOwners.remove(uploadId);
    }

    /**
     * 静默清理分片目录及其上传者记录，失败不影响主流程
     */
    private void cleanChunksQuietly(String uploadId, File chunkDir){
        deleteDirectory(chunkDir);
        uploadOwners.remove(uploadId);
    }

    /**
     * 确保分片目录存在。
     * 并发上传时多个线程可能同时创建同一目录，mkdirs在目录已被其他线程创建时返回false，
     * 因此失败后需要重新判断目录是否已存在，避免误报创建失败。
     *
     * @return 分片目录，创建失败返回null
     */
    private File ensureChunkDir(String uploadId){
        File chunkDir=new File(getChunkRoot(),uploadId);
        if(chunkDir.isDirectory())
            return chunkDir;
        if(chunkDir.exists())
            return null;
        //并发下mkdirs可能因其他线程已创建而返回false，需二次确认
        if(!chunkDir.mkdirs()&&!chunkDir.isDirectory())
            return null;
        return chunkDir;
    }

    private boolean uploadBelongsToUser(String uploadId,Integer userId){
        Integer owner = uploadOwners.get(uploadId);
        return owner!=null&&owner.equals(userId);
    }

    private boolean validMergedVideo(String videoName,Integer userId){
        if(videoName==null||!videoName.matches("[A-Za-z0-9_-]+\\.[A-Za-z0-9]+"))
            return false;
        Integer owner = mergedVideoOwners.get(videoName);
        if(owner==null||!owner.equals(userId))
            return false;
        return Files.exists(Paths.get(FilePathEnum.UPLOAD_VIDEO.getPath(), videoName));
    }

    private void deleteOldVideoFile(Integer videoId){
        Videos video = videosMapper.selectById(videoId);
        if(video==null||video.getVideoAddress()==null)
            return;
        //远程视频没有本地文件
        if(video.getVideoSource()!=null&&video.getVideoSource()==1)
            return;
        String videoAddress = video.getVideoAddress();
        int indexOf = videoAddress.lastIndexOf('/');
        String videoFile = (indexOf != -1) ? videoAddress.substring(indexOf + 1) : videoAddress;
        try {
            Files.deleteIfExists(Paths.get(FilePathEnum.UPLOAD_VIDEO.getPath() + videoFile));
        } catch (IOException e) {
        }
    }

    private void deleteDirectory(File file){
        if(file==null||!file.exists())
            return;
        File[] files = file.listFiles();
        if(files!=null){
            for (File child : files) {
                deleteDirectory(child);
            }
        }
        file.delete();
    }


}

