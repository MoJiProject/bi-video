package com.moji.controller;
import com.moji.FilePathEnum;
import com.moji.R;
import com.moji.mapper.VideosMapper;
import com.moji.po.Videos;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.VideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private VideosService videosService;

    @Autowired
    private VideosMapper videosMapper;


    /**
     * 上传视频
     * @param files
     * @param uid
     * @param videos
     * @return
     */
    @PostMapping("/uploadVideo")
    public R<String> uploadMedia(@RequestParam("file") MultipartFile[] files,@RequestParam(value = "uId",required = false) Integer uid, @ModelAttribute Videos videos,@RequestHeader("Authorization") String token) {

        String videoName=null;
        String coverName=null;
        if(uid!=null)
            if (uid == 0) {
                return R.success("");
            }

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(videos.getUserId(),token))
            return R.error("操作失败");

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

                // 创建目录（如果不存在）
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
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
                    coverName = fileName + extension; // 新的文件名保留扩展名
                    File dest = new File(uploadDir, coverName);
                    file.transferTo(dest);
                }
            } catch (Exception e) {
                return R.error("文件上传失败: " );
            }
        }

        Boolean b = videosService.insertVideo(videoName, coverName, videos);
        if (b)
            return R.success("上传成功");

        return R.error("上传失败");

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
    public R<String> updateVideo(@RequestParam(value = "file",required = false) MultipartFile[] files,@RequestParam(value = "userId",required = false) Integer uid, @ModelAttribute Videos videos,@RequestHeader("Authorization") String token) {

        String videoName = null;
        String coverName = null;
        boolean vFlag=false;
        boolean cFlag=false;
        if (uid == 0) {
            return R.success("你还没有提交参数");
        }

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(videos.getUserId(),token))
            return R.error("操作失败");

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

                    // 创建目录（如果不存在）
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
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
                        coverName = fileName + extension; // 新的文件名保留扩展名
                        File dest = new File(uploadDir, coverName);
                        file.transferTo(dest);
                    }


                } catch (Exception e) {
                    return R.error("文件上传失败: ");
                }
            }
        }
        Boolean flag = videosService.updateVideo(videoName, coverName, videos,vFlag,cFlag);
        if (flag)
            return R.success("修改成功");

        return R.error("修改失败");

    }


}

