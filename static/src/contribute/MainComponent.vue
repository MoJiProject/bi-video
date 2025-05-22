<template>
  <div class="main-content4" v-loading="loading">
    <div class="white">
      <div
        style="
          transform: translate(505px, 20px);
          height: 40px;
          width: 1200px;
          background-color: white;
          color: #00a1d6;
          font-weight: 800;
        "
      >
        视频投稿
        <div
          style="
            width: 1200px;
            height: 3px;
            background-color: #f8f8f8;
            transform: translate(-600px, 20px);
          "
        ></div>
        <div
          style="
            width: 65px;
            height: 3px;
            background-color: #00a1d6;
            transform: translate(0px, 17px);
          "
        ></div>
      </div>
      <div
        v-if="fileListVideo.length === 1"
        style="font-weight: 800; margin-left: 40px; margin-top: 60px"
      >
        发布视频
      </div>
      <div
        v-if="fileListVideo.length === 1"
        style="
          width: 800px;
          height: 150px;
          background-color: #f8f8f8;
          transform: translate(75px, 30px);
        "
      >
       <div style="transform: translateY(-25%);">
        <div style="font-size: 14px; transform: translate(50px, 60px);width: 700px;">
          {{ fileVideoname }}
        </div>
        <div
          style="
            color: #9a9a9a;
            font-size: 10px;
            transform: translate(50px, 65px);
          "
        >
          已经上传：{{ formatFileSize(videoSize) }}/{{
            formatFileSize(videoSize)
          }}
        </div>
        <img
          @click="reVideo"
          src="../img/重新选择.png"
          style="transform: translate(670px, 50px); position: absolute;cursor: pointer;"
        />
        <img
          src="../img/uploadVideoImg.png"
          style="width: 28px; transform: translate(10px, 30px)"
        />
        <el-progress
          :percentage="progress"
          style="width: 700px; transform: translate(50px, 30px)"
        />
       </div>
      </div>

      <el-upload
        v-show="fileListVideo.length === 0"
        style="transform: translate(130px, 40px); width: 850px;"
        ref="uploadRef2"
        class="upload-demo"
        :headers="{
                'Authorization': store.token
              }"
        action="/api/upload/uploadVideo"
        :data="{ uId: 0 }"
        show-file-list
        drag
        accept="video/mp4,video/x-m4v,video/*"
        :file-list="fileListVideo"
        :before-upload="beforeUploadVideo"
        :on-change="handleChangeVideo"
        :on-remove="handleRemoveVideo"
        :limit="1"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          <img
            src="../img/upload.png"
            style="
              width: 34px;
              transform: translate(-35px, -95px);
              position: absolute;
            "
          />
          <span
            style="
              font-size: 12px;
              color: #b3b3b3;
              transform: translate(345px, -55px);
              display: flex;
            "
            >拖拽到此处也可上传</span
          >
          <div
            style="
              transform: translate(308px, -35px);
              position: absolute;
              display: flex;
              justify-content: center;
              align-items: center;
              background-color: #00a1d6;
              width: 180px;
              border-radius: 5px;
              height: 40px;
            "
          >
            <span style="color: white">上传视频</span>
          </div>
        </div>
      </el-upload>

      <div
        v-if="fileListVideo.length === 1"
        style="
          font-weight: 800;
          margin-left: 40px;
          margin-top: 60px;
          transform: translateY(20px);
        "
      >
        基本设置
      </div>

      <div
        v-if="fileListVideo.length === 0"
        style="
          transform: translate(275px, 200px);
          display: flex;
          flex-direction: column;
        "
      >
        <div style="transform: translate(210px, -15px)">
          <span style="display: flex; transform: translate(-130px, 23px)"
            ><a
              class="forbind"
              href="https://www.bilibili.com/blackboard/blackroom.html"
              target="_blank"
              style="font-size: 12px; text-decoration: none; color: #9c9f9f"
              >禁止发布的视频内容</a
            ></span
          >
          <el-popover
            :show-arrow="false"
            placement="top-start"
            :width="300"
            trigger="hover"
            content="网页端上传的文件大小上限为16G
                  视频内容时长最大10小时
                  （提升电磁力，即可体验64G超大文件上限哦！前往创作实验室了解更多）
                  过长或过大视频建议拆分后使用分p或合集功能进行投稿～"
          >
            <template #reference>
              <el-button
                class="m-2"
                style="border: none; font-size: 12px; color: #9c9f9f;position: relative;"
                >视频大小</el-button
              >
            </template>
          </el-popover>
          <el-popover
            :show-arrow="false"
            placement="top-start"
            :width="300"
            trigger="hover"
            content="网页端、桌面客户端推荐上传的格式为：mp4,flv
                （推荐上传的格式在转码过程更有优势，审核过程更快哦！）
                其他允许上传的格式：mp4,flv,avi,wmv,mov,webm,mpeg4,ts,mpg,rm,rmvb,mkv,m4v"
          >
            <template #reference>
              <el-button
                class="m-2"
                style="border: none; font-size: 12px; color: #9c9f9f;position: relative;"
                >视频格式</el-button
              >
            </template>
          </el-popover>
          <el-popover
            :show-arrow="false"
            placement="top-start"
            :width="300"
            trigger="hover"
            content="分辨率最大支持 8192*4320
                推荐视频分辨率：1920*1080 或者 3840*2160
                推荐视频码率：1080p大于6000kbps；4k大于20000kbps；8k大于40000kbps
                音频采样率48000Hz
                推荐音频码率：320kbps
                逐行扫描
                智能识别输出HDR
                智能识别全景视频"
          >
            <template #reference>
              <el-button
                class="m-2"
                style="border: none; font-size: 12px; color: #9c9f9f;position: relative;"
                >视频码率</el-button
              >
            </template>
          </el-popover>
        </div>

        <div class="forter2" style="font-size: 12px; color: #9c9f9f">
          上传视频，即表示您已同意
          <a
            style="font-size: 12px; color: #00a1d6; text-decoration: none"
            target="_blank"
            href="https://member.bilibili.com/platform/upload/video/frame?page_from=creative_home_top_upload#:~:text=%E6%82%A8%E5%B7%B2%E5%90%8C%E6%84%8F-,%E5%93%94%E5%93%A9%E5%93%94%E5%93%A9%E4%BD%BF%E7%94%A8%E5%8D%8F%E8%AE%AE,-%E4%B8%8E%20%E5%93%94%E5%93%A9"
            >哔哩哔哩使用协议</a
          >
          与
          <a
            style="font-size: 12px; color: #00a1d6; text-decoration: none"
            target="_blank"
            href="https://member.bilibili.com/platform/upload/video/frame?page_from=creative_home_top_upload#:~:text=%E4%BD%BF%E7%94%A8%E5%8D%8F%E8%AE%AE%20%E4%B8%8E-,%E5%93%94%E5%93%A9%E5%93%94%E5%93%A9%E7%A4%BE%E5%8C%BA%E5%85%AC%E7%BA%A6,-%EF%BC%8C%E8%AF%B7%E5%8B%BF%E4%B8%8A"
            >哔哩哔哩社区公约</a
          >
          ，请勿上传色情，反动等违法视频。
        </div>

        <div style="margin-top: 20px; margin-left: 67px">
          <span style="color: #9c9f9f; font-size: 12px">第三方工具</span>
          <span
            style="
              color: #9c9f9f;
              font-size: 12px;
              margin-left: 30px;
              cursor: pointer;
            "
            ><img
              src="../img/小丸压.png"
              style="transform: translate(2px, 3px); width: 16px"
            />小丸压制工具原版</span
          >
          <span
            style="
              color: #9c9f9f;
              font-size: 12px;
              margin-left: 30px;
              cursor: pointer;
            "
            ><img
              src="../img/小丸压定制版.png"
              style="transform: translate(2px, 3px); width: 16px"
            />小丸压制工具bilibili定制版</span
          >
        </div>
      </div>

      <el-form
        v-show="fileListVideo.length !== 0"
        :model="video"
        label-width="auto"
        style="max-width: 600px"
      >
        <div style="margin-left: 28px; margin-top: 20px">
          <span style="font-size: 14px"
            ><span style="color: red">*&nbsp;</span>封面</span
          >
          <span
            style="
              transform: translate(280px, 50px);
              position: absolute;
              font-size: 12px;
              color: #878787;
            "
            >系统默认选中第一帧为视频封面</span
          >
          <div @click="centerDialogVisible = true" class="cover">选择封面</div>
          <img
            v-if="fileListCover.length === 0"
            :src="thumbnail || selectedThumbnail"
            alt="Video Thumbnail"
            style="
              width: 169px;
              height: 127px;
              object-fit: cover;
              transform: translate(83px, 40px);
              z-index: 0;
            "
          />
          <img
            v-if="fileListCover.length === 1"
            :src="getImageUrl(fileListCover[0])"
            alt="Video Thumbnail"
            style="
              width: 169px;
              object-fit: cover;
              height: 127px;
              transform: translate(83px, 40px);
              z-index: 0;
            "
          />

          <el-dialog
            style="transform: translate(-55px, -20px); height: 700px"
            :modal="false"
            :lock-scroll="false"
            v-model="centerDialogVisible"
            width="700"
            center
          >
            <div
              @click="coverImg = 1"
              style="
                cursor: pointer;
                display: inline-block;
                transform: translate(120px, -10px);
                font-size: 16px;
                color: #00a1d6;
              "
            >
              上传封面
            </div>
            <div
              @click="coverImg = 0"
              style="
                cursor: pointer;
                display: inline-block;
                transform: translate(-50px, -10px);
                font-size: 16px;
                color: #00a1d6;
              "
            >
              截取封面
            </div>
            <span>
              <div
                style="
                  width: 700px;
                  height: 3px;
                  background-color: #f8f8f8;
                  transform: translate(-16px, 10px);
                "
              ></div>
              <div
                v-if="coverImg === 0"
                style="
                  width: 66px;
                  height: 3px;
                  background-color: #00a1d6;
                  transform: translate(14px, 7px);
                "
              ></div>
              <div
                v-if="coverImg === 1"
                style="
                  width: 66px;
                  height: 3px;
                  background-color: #00a1d6;
                  transform: translate(119px, 7px);
                "
              ></div>
            </span>
            <el-upload
              v-if="coverImg === 1"
              ref="uploadRef"
              drag
              :headers="{
                'Authorization': store.token
              }"
              action="/api/upload/uploadVideo"
              :data="{ uId: 0 }"
              accept="image/jpeg,image/png,image/gif,image/bmp,image/webp"
              :file-list="fileListCover"
              :before-upload="beforeUploadCover"
              :on-change="handleChangeCover"
              :on-remove="handleRemoveCover"
              style="width: 550px; transform: translate(60px, 40px)"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                <img
                  src="../img/uploadCover.png"
                  style="width: 100px; transform: translate(-10px, -70px)"
                />
                <div
                  style="
                    color: #00a1d6;
                    font-size: 12px;
                    transform: translate(-10px, -50px);
                  "
                >
                  拖拽图片到此或点击上传
                </div>
                <div
                  style="
                    color: #b3b3b3;
                    font-size: 12px;
                    transform: translate(-10px, -40px);
                  "
                >
                  建议上传4:3比例高清图片，尺寸≥1200*900
                </div>
              </div>
            </el-upload>
            <div v-if="coverImg === 0" style="transform: translate(15px, 25px)">
              拖拽选框裁剪
            </div>
            <CropperCompVue
              v-if="coverImg === 0"
              @cut="handleCut"
              :imgURL="thumbnail || selectedThumbnail"
              :title="fileNameWithoutExtension"
              style="transform: translate(20px, 20px)"
            />
            <div v-if="coverImg === 0" @click="defaultCover" class="crop">
              使用封面模板
            </div>
            <div v-show="coverImg === 0" class="thumbnail-slider">
              <div class="thumbnails" ref="thumbnailsRef">
                <img
                  v-for="(thumb, index) in thumbnails"
                  :key="index"
                  :src="thumb"
                  @click="selectThumbnail(index)"
                  class="thumbnail"
                />
              </div>
            </div>
            <template #footer>
              <div
                v-if="coverImg === 1 && fileListCover.length === 0"
                class="uploadCoverImg"
                @click="handleUploadClick"
              >
                <span style="color: white">上传图片</span>
              </div>
              <div
                v-if="coverImg === 1 && fileListCover.length === 1"
                class="uploadCoverImg"
                @click="centerDialogVisible = false"
              >
                <span style="color: white">完成</span>
              </div>
            </template>
          </el-dialog>
        </div>

        <div style="margin-left: 28px; margin-top: 90px">
          <span style="font-size: 14px"
            ><span style="color: red">*&nbsp;</span>标题</span
          >
          <el-input
            v-model="video.title"
            style="
              width: 700px;
              height: 40px;
              transform: translate(120px, -30px);
            "
            maxlength="80"
            placeholder="标题建议提炼内容亮点"
            show-word-limit
            type="text"
          />
        </div>
        <div style="margin-left: 27px; margin-top: -10px">
          <span style="font-size: 14px"
            ><span style="color: red">*&nbsp;</span>类型</span
          >
          <el-radio-group
            class="contribute-type"
            v-model="video.type"
            style="transform: translate(85px, 0px);"
          >
            <el-radio class="contribute-radio" :value="0">自制</el-radio>
            <el-radio class="contribute-radio" :value="1">转载</el-radio>
          </el-radio-group>
        </div>

        <div style="margin-left: 28px; margin-top: 30px">
          <span style="font-size: 14px"
            ><span style="color: red">*&nbsp;</span>分区</span
          >
          <div style="transform: translate(120px, -27px)">
            <el-select
              v-model="video.subZoneKey"
              placeholder="请选择分区"
              style="width: 240px"
              @change="handleCountryChange"
            >
              <el-option
                v-for="country in countries"
                :key="country.value"
                :label="country.label"
                :value="country.value"
              />
            </el-select>

            <el-select
              v-model="video.subZoneValue"
              placeholder="请选择分类"
              style="width: 240px"
              :disabled="!video.subZoneKey"
            >
              <el-option
                v-for="city in filteredCities"
                :key="city.value"
                :label="city.label"
                :value="city.value"
              />
            </el-select>
          </div>
        </div>

        <div
          style="
            margin-left: 28px;
            margin-top: 20px;
            height: auto;
            width: 850px;
          "
        >
          <span style="font-size: 14px"
            ><span style="color: red">*&nbsp;</span>标签</span
          >
          <el-tag
            v-for="tag in dynamicTags"
            :key="tag"
            closable
            @close="handleClose(tag)"
            style="
              transform: translateX(80px);
              background-color: #00a1d6;
              color: white;
            "
          >
            &nbsp;&nbsp; {{ tag }}
          </el-tag>
          <el-input
            v-if="dynamicTags.length !== 5"
            v-model="inputValue"
            @blur="handleInputConfirm"
            @keyup.enter="handleInputConfirm"
            ref="InputRef"
            placeholder="按回车键Enter创建标签"
            size="small"
            style="width: 200px; margin-left: 85px; height: 30px"
            @input="removeSpaces"
          >
          </el-input>
        </div>

        <div
          style="
            margin-left: 40px;
            margin-top: 60px;
            height: 200px;
            width: 200px;
          "
        >
          <span style="font-size: 14px">简介</span>
          <span v-show="placeholderFlag" class="placeholder">视频类型：动漫杂谈<br/>相关题材：可填写多个<br/>简介：...</span>
          <span class="content-length">{{ content.length }}/2000</span>
          <div class="content-input" @click="handleContent" @input="handleContent" contenteditable="true" 
          @keyup.left="handleContent" @keyup.right="handleContent" @keyup.up="handleContent" @keyup.down="handleContent">
          </div>
          <div v-show="eitFlag" class="eit-container" 
          :style="{
            left: eitLocationX-284+'px',
            top: eitLocationY-330+'px',
          }">
            <eit :eitList="eitList" :eitKeyWord="eitKeyWord"/>
          </div>
        </div>

        <div
          style="
            margin-top: 30px; 
            margin-left: 40px;
            margin-bottom: 60px;
            height: 200px;
            width: 200px;
          "
        >
          <span style="font-size: 14px">二创设置</span
          ><span
            style="
              font-size: 14px;
              position: absolute;
              transform: translate(110px, 5px);
            "
            >允许二创</span
          >
          <el-switch v-model="video.allowTwo" style="margin-left: 55px" />
        </div>

        <div class="commit" @click="uploadFile">
          <span style="color: white; font-size: 14px">立即投稿</span>
        </div>
      </el-form>

      <el-dialog
        v-model="dialogVisible"
        title="确定要离开吗？"
        width="392"
        center
        :modal="false"
        style="transform: translate(-80px, 80px); height: 167px"
      >
        <span
          style="
            transform: translate(45px, 10px);
            position: absolute;
            font-size: 12px;
          "
        >
          系统可能不会保存填写的稿件信息噢...(´；ω；`)
        </span>
        <template #footer>
          <div class="dialog-footer" style="transform: translate(0px, 40px)">
            <el-button
              @click="confirmLeave"
              style="
                background-color: #00a1d6;
                color: white;
                border: none;
                width: 90px;
                height: 28px;
              "
              >确定</el-button
            >
            <el-button
              type="primary"
              @click="cancelLeave"
              style="
                background-color: white;
                color: black;
                border: 1px solid #b3b3b3;
                width: 90px;
                height: 28px;
              "
              >取消</el-button
            >
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import apiClient from "../services/apiClient";
import "element-plus/theme-chalk/el-message.css";
import { reactive, onMounted, ref, computed, nextTick, watch, onUnmounted } from "vue";
import { ElMessage } from "element-plus";
import {useGlobalStore} from "../store/store";
import CropperCompVue from "./CropperComp.vue";
import eit from "../components/eit.vue";
export default {
  name: "MainComponent",
  components: {
    CropperCompVue,
    eit,
  },
  setup() {
    const store = useGlobalStore();
    const loading = ref(false);
    const storer=reactive(store);
    const dialogVisible = ref(false);
    let nextRoute = null;
    const inputValue = ref("");
    const inputVisible = ref(false);
    const InputRef = ref(null);
    const dynamicTags = ref([]); // 使用 dynamicTags 初始化为空数组
    const MAX_TAGS = 5; // 最大标签数量
    const fileListCover = ref([]);
    const fileListVideo = ref([]);
    const fileVideoname = ref("");
    const progress = ref(100);
    const videoSize = ref(0);
    const coverImg = ref(0);
    const content = ref("");
    const placeholderFlag = ref(true);
    const centerDialogVisible = ref(false);
    const eitLocationX=ref(null);
    const eitLocationY=ref(null);
    const eitFlag=ref(false);
    const eitList=reactive({});
    const eitKeyWord=ref("");
    const eitUserNames=ref([]);
    const video = reactive({
      id: 0,
      userId: 0,
      tag: [],
      title: "",
      content: "",
      videoAddress: "",
      likeNumber: 0,
      commentNumber: 0,
      collectNumber: 0,
      shareNumber: 0,
      createTime: "",
      coverAddress: "",
      coinThrowNumber: 0,
      allowTwo: false,
      type: 0,
      subZoneKey: "",
      subZoneValue: "",
      playNumber: 0,
      status: 0,
      newFansNumber: 0,
      scrollingNumber: 0,
      videoTime: "",
    });
    const uploadRef = ref(null);
    const uploadRef2 = ref(null);
    const thumbnail = ref(null);
    const fileNameWithoutExtension = ref("");
    const thumbnails = ref([]);
    const selectedThumbnail = ref(null); // 用于保存选中的缩略图
    const temp = ref(null);
    function defaultCover() {
      removeFileCover(0);
      centerDialogVisible.value = false;
      thumbnail.value = temp.value;
    }
    const beforeRouteLeave = (to, from, next) => {
      if (fileListVideo.value.length > 0) {
        // 检查条件
        nextRoute = next; // 保存即将离开的路由
        dialogVisible.value = true; // 显示对话框
        next(false); // 取消路由导航
      } else {
        next(); // 允许导航
      }
    };

    //用户离开
    const confirmLeave = () => {
      successUploadVideo(); // 处理成功上传逻辑
      dialogVisible.value = false; // 关闭对话框

      if (nextRoute) {
        nextRoute(); // 确认后导航到下一个路由

        nextRoute = null; // 清空 nextRoute，防止重复调用
      }
    };

    const cancelLeave = () => {
      dialogVisible.value = false; // 关闭对话框
    };
    // 判断视频是否符合
    const beforeUploadVideo = (file) => {
      const maxSize = 300 * 1024 * 1024; // 300MB
      const validTypes = [
        "video/mp4",
        "video/x-m4v",
        "video/quicktime",
        "video/ogg",
        "video/webm",
      ]; // 允许的视频格式

      // 检查文件大小
      if (file.size > maxSize) {
        ElMessage({
          message: "不能超过300MB哦",
          type: "info",
          plain: true,
          duration: 1700,
        });
        return false; // 阻止上传
      }

      // 检查文件类型
      if (!validTypes.includes(file.type)) {
        ElMessage({
          message: "只能上传视频哦",
          type: "info",
          plain: true,
          duration: 1700,
        });
        return false; // 阻止上传
      }

      return true; // 返回 true 继续上传
    };
    // 判断视频封面是否符合
    const beforeUploadCover = (file) => {
      const maxSize = 10 * 1024 * 1024; // 10MB
      const validTypes = [
        "image/jpeg",
        "image/png",
        "image/gif",
        "image/bmp",
        "image/webp",
      ];

      if (fileListCover.value.length > 1) {
        ElMessage({
          message: "不能超过1个哦",
          type: "info",
          plain: true,
          duration: 1700,
        });
        return false; // 阻止上传
      }
      // 检查文件大小
      if (file.size > maxSize) {
        ElMessage({
          message: "文件不能超过10MB哦",
          type: "info",
          plain: true,
          duration: 1700,
        });
        return false; // 阻止上传
      }

      // 检查文件类型
      if (!validTypes.includes(file.type)) {
        ElMessage({
          message: "只能是图片哦",
          type: "info",
          plain: true,
          duration: 1700,
        });
        return false; // 阻止上传
      }
      return true;
    };
    function handleCut(base64Image) {
      removeFileCover(0);
      thumbnail.value = base64Image;
      centerDialogVisible.value = false;
    }

    onMounted(() => {
      window.scrollTo({top: 0, behavior: "smooth"});
      getEitList();
      window.addEventListener("click", handleClickOutside);
      window.addEventListener("beforeunload",handleOnload);
      
    });

    onUnmounted(() => {
      window.removeEventListener("click", handleClickOutside);
      window.removeEventListener("beforeunload",handleOnload);
    })

    function handleOnload(event){
      if(fileListVideo.value.length>0)
       {
        window.addEventListener("beforeunload",handleOnload);
        event.preventDefault();
      }
      if(fileListVideo.value.length===0)
      window.removeEventListener("beforeunload",handleOnload);
       
    }

    function handleClickOutside(event) {
      if (!(event.target.className === "content-input"||event.target.className === "placeholder"||event.target.className === "eit-container"||event.target.className === "content-length"))
        eitFlag.value=false;
    }
  
    //重新上传视频
    function reVideo() {
      handleRemoveVideo();
      removeFileVideo(0);
      handleUploadClickVideo();
      removeFileCover(0);
    }

    //视频上传成功的处理
    function successUploadVideo() {
      handleRemoveVideo();
      removeFileVideo(0);
      removeFileCover(0);
      cleanVideoData();
    }
    //获取视频名字和获取视频缩略图
    const handleChangeVideo = (file, fileList) => {
      fileVideoname.value = file.name;
      const fileVideoNamebefore = file.name;
      const lastDotIndex = fileVideoNamebefore.lastIndexOf(".");
      fileNameWithoutExtension.value =
        lastDotIndex !== -1
          ? fileVideoNamebefore.substring(0, lastDotIndex) // 使用 substring 而不是括号
          : fileVideoNamebefore;

      // 直接将去除后缀的文件名赋值给 video.title
      video.title = fileNameWithoutExtension.value;

      videoSize.value = file.size;
      fileListVideo.value = fileList;
      if (fileList.length > 0) {
        const videoFile = fileList[0].raw; // 获取原始文件对象

        // 创建视频元素
        const video = document.createElement("video");
        video.src = URL.createObjectURL(videoFile);
        video.preload = "metadata"; // 预加载元数据

        video.onloadedmetadata = () => {
          // 设置视频播放时间到0秒
          video.currentTime = 0;

          // 等待视频加载第一帧
          video.onseeked = () => {
            // 创建canvas元素
            const canvas = document.createElement("canvas");
            canvas.width = video.videoWidth;
            canvas.height = video.videoHeight;
            const ctx = canvas.getContext("2d");

            // 将视频的当前帧绘制到canvas上
            ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

            // 获取Base64图像
            thumbnail.value = canvas.toDataURL("image/jpeg");
            temp.value = canvas.toDataURL("image/jpeg");
          };
        };
      }
      const videoUrl = URL.createObjectURL(file.raw);
      const videoElement = document.createElement("video");
      videoElement.src = videoUrl;

      videoElement.addEventListener("loadeddata", () => {
        const duration = Math.floor(videoElement.duration);
        const canvas = document.createElement("canvas");
        const ctx = canvas.getContext("2d");

        videoElement.pause(); // 确保视频暂停

        const extractThumbnails = (i) => {
          if (i < duration) {
            videoElement.currentTime = i; // 设置当前时间为 i 秒

            const seekedHandler = () => {
              // 仅在 seeked 事件中处理图像
              canvas.width = videoElement.videoWidth;
              canvas.height = videoElement.videoHeight;

              ctx.drawImage(videoElement, 0, 0, canvas.width, canvas.height);
              const dataUrl = canvas.toDataURL();
              thumbnails.value.push(dataUrl);

              // 条件判断，如果满足条件则移除事件监听器
              if (fileListVideo.value.length === 0) {
                videoElement.removeEventListener("seeked", seekedHandler);
                thumbnails.value = [];
                selectedThumbnail.value = null;
                thumbnail.value = null;
                return; // 结束提取
              }

              // 递归调用下一秒
              setTimeout(() => extractThumbnails(i + 1), 200); // 增加延迟确保稳定性
            };

            videoElement.addEventListener("seeked", seekedHandler, {
              once: true,
            });
          }
        };

        extractThumbnails(0); // 从0秒开始提取
      });
    };
    //清空视频数据
    const handleRemoveVideo = (file) => {
      // 当文件被移除时，从文件列表中删除该文件
      const index = fileListVideo.value.indexOf(file);
      removeFileVideo(index);
      thumbnails.value = [];
      selectedThumbnail.value = null;
      thumbnail.value = null;
    };
    //清空视频数据
    const removeFileVideo = (index) => {
      if (index > -1) {
        fileListVideo.value.splice(index, 1); // 从文件列表中删除指定文件
      }
    };
    //
    const formatFileSize = (size) => {
      if (size < 1024) return size + " B";
      else if (size < 1024 * 1024) return (size / 1024).toFixed(2) + " KB";
      else if (size < 1024 * 1024 * 1024)
        return (size / (1024 * 1024)).toFixed(2) + " MB";
      else return (size / (1024 * 1024 * 1024)).toFixed(2) + " GB";
    };

    const handleChangeCover = (file, fileList) => {
      // 更新文件列表
      fileListCover.value = fileList;
    };

    const handleRemoveCover = (file) => {
      // 当文件被移除时，从文件列表中删除该文件
      removeFileCover(fileListCover.value.indexOf(file));
    };

    const removeFileCover = (index) => {
      if (index > -1) {
        fileListCover.value.splice(index, 1); // 从文件列表中删除指定文件
      }
    };

    // 手动触发封面选择框
    const handleUploadClick = () => {
      if (uploadRef.value) {
        uploadRef.value.$el.querySelector('input[type="file"]').click();
      } else {
        console.error("uploadRef 为空或未正确引用");
      }
    };
    // 手动触发视频文件选择框
    const handleUploadClickVideo = () => {
      if (uploadRef2.value) {
        uploadRef2.value.$el.querySelector('input[type="file"]').click();
      } else {
        console.error("uploadRef2 为空或未正确引用");
      }
    };
    //发送请求上传视频
    const uploadFile = async () => {
      // 确保有文件可上传

      if (fileListCover.value.length === 0 && thumbnail.value === null) {
        ElMessage({
          message: "请上传封面",
          type: "info",
          offset: 376,
        });
        return;
      }

      if (video.content.length < 5) {
        ElMessage({
          message: "内容太短了",
          type: "info",
          offset: 376,
        });
        return;
      }
      const file = fileListVideo.value[0]; // 获取选中的视频文件
      const formData = new FormData();
      formData.append("file", file.raw); // 使用 file.raw 获取视频文件

      const dataImg = thumbnail.value.split(",")[1]; // 去掉前缀部分
      const blob = await (
        await fetch(`data:image/jpeg;base64,${dataImg}`)
      ).blob();

      // 使用了默认封面
      if (fileListCover.value.length === 0) {
        formData.append("file", blob, "thumbnail.jpg");
      } else {
        // 如果没有使用默认封面
        const file2 = fileListCover.value[0];
        formData.append("file", file2.raw);
      }

      await publishComment();
      // 填充视频信息
      video.userId = store.userId;
      video.tag = dynamicTags.value;
      if (video.allowTwo) {
        video.allowTwo = 1;
      } else video.allowTwo = 0;
      // 将视频信息逐个属性添加到 FormData
      for (const key in video) {
        if (Array.isArray(video[key])) {
          video[key].forEach((tag) => formData.append(`${key}[]`, tag)); // 处理数组属性
        } else {
          formData.append(key, video[key]); // 处理单一属性
        }
      }
      loading.value = true;
      try {
        const response = await apiClient.post(
          "/upload/uploadVideo",
          formData,
          {
            headers: {
              "Content-Type": "multipart/form-data",
              "Authorization": store.token,
            },
          },
        );
        if (response.data.data === "上传成功") {
          ElMessage({
            message: "视频上传成功",
            type: "info",
            offset: 300,
          });
          window.removeEventListener("beforeunload",handleOnload);
          loading.value = false;
          successUploadVideo();
          cleanVideoData();
          window.scrollTo({top: 0, behavior: "smooth"});
        } else {
          loading.value = false;
          ElMessage({
            message: response.data.msg,
            type: "info",
            offset: 300,
          });
          location.reload();
        }
      } catch (error) {
        loading.value = false;
        ElMessage({
          message: "未知错误",
          type: "info",
          offset: 300,
        });
      }
    };

    const selectedCountry = ref("");
    const selectedCity = ref("");
    //分类父数据
    const countries = [
      { label: "生活", value: "生活" },
      { label: "游戏", value: "游戏" },
      { label: "娱乐", value: "娱乐" },
      { label: "知识", value: "知识" },
      { label: "影视", value: "影视" },
      { label: "音乐", value: "音乐" },
      { label: "动画", value: "动画" },
      { label: "时尚", value: "时尚" },
      { label: "美食", value: "美食" },
      { label: "汽车", value: "汽车" },
      { label: "运动", value: "运动" },
      { label: "科技", value: "科技" },
      { label: "动物圈", value: "动物圈" },
      { label: "舞蹈", value: "舞蹈" },
      { label: "国创", value: "国创" },
      { label: "鬼畜", value: "鬼畜" },
      { label: "纪录片", value: "纪录片" },
      { label: "番剧", value: "番剧" },
      { label: "电视剧", value: "电视剧" },
      { label: "电影", value: "电影" },
    ];
    //分类子数据
    const cities = {
      "生活": [
        { label: "出行", value: "出行" },
        { label: "搞笑", value: "搞笑" },
        { label: "亲子", value: "亲子" },
        { label: "三农", value: "三农" },
        { label: "家居房产", value: "家居房产" },
        { label: "手工", value: "手工" },
        { label: "绘画", value: "绘画" },
        { label: "日常", value: "日常" },
      ],
      "游戏": [
        { label: "单机游戏", value: "单机游戏" },
        { label: "网络游戏", value: "网络游戏" },
        { label: "手机游戏", value: "手机游戏" },
        { label: "电子竞技", value: "电子竞技" },
        { label: "桌游棋牌", value: "棋牌桌游" },
        { label: "音游", value: "音游" },
        { label: "GMV", value: "GMV" },
        { label: "Mugen", value: "Mugen" },
      ],
      "娱乐": [
        { label: "娱乐杂谈", value: "娱乐杂谈" },
        { label: "CP安利", value: "CP安利" },
        { label: "颜值安利", value: "颜值安利" },
        { label: "娱乐粉丝创作", value: "娱乐粉丝创作" },
        { label: "娱乐资讯", value: "明星资讯" },
        { label: "明星综合", value: "明星综合" },
        { label: "综艺", value: "综艺" },
      ],
      "知识": [
        { label: "科学科普", value: "科学科普" },
        { label: "社科法律心理", value: "社科法律心理" },
        { label: "人文历史", value: "人文历史" },
        { label: "财经商业", value: "财经商业" },
        { label: "校园学习", value: "校园学习" },
        { label: "职业职场", value: "职业职场" },
        { label: "设计·创业", value: "设计·创业" },
        { label: "野生技能协会", value: "野生技能协会" },
      ],
      "影视": [
        { label: "影视整活", value: "影视整活" },
        { label: "Ai影像", value: "Ai影像" },
        { label: "预告资讯", value: "预告资讯" },
        { label: "小剧场", value: "小剧场" },
        { label: "短片", value: "短片" },
        { label: "影视综合", value: "影视综合" },
        { label: "影视杂谈", value: "影视杂谈" },
        { label: "影视剪辑", value: "影视剪辑" },
      ],
      "音乐": [
        { label: "原创音乐", value: "原创音乐" },
        { label: "翻唱", value: "翻唱" },
        { label: "演奏", value: "演奏" },
        { label: "月评盘点", value: "月评盘点" },
        { label: "VOCALOID·UTAU", value: "VOCALOID·UTAU" },
        { label: "MV", value: "MV" },
        { label: "音乐粉丝饭拍", value: "音乐粉丝饭拍" },
        { label: "Ai音乐", value: "Ai音乐" },
        { label: "电台", value: "电台" },
        { label: "音乐教学", value: "音乐教学" },
        { label: "音乐综合", value: "音乐综合" },
      ],
      "动画": [
        { label: "MAD·AMV", value: "MAD·AMV" },
        { label: "MMD·3D", value: "MMD·3D" },
        { label: "同人手书", value: "同人手书" },
        { label: "配音", value: "配音" },
        { label: "模玩周边", value: "模玩周边" },
        { label: "特摄", value: "特摄" },
        { label: "动漫杂谈", value: "动漫杂谈" },
        { label: "综合", value: "综合" },
      ],
      "时尚": [
        { label: "美妆护肤", value: "美妆护肤" },
        { label: "仿妆cos", value: "仿妆cos" },
        { label: "穿搭", value: "穿搭" },
        { label: "时尚潮流", value: "时尚潮流" },
      ],
      "美食": [
        { label: "美食制作", value: "美食制作" },
        { label: "美食侦探", value: "美食侦探" },
        { label: "美食测评", value: "美食测评" },
        { label: "田园美食", value: "田园美食" },
        { label: "美食记录", value: "美食记录" },
      ],
      "汽车": [
        { label: "汽车知识科普", value: "汽车知识科普" },
        { label: "购车攻略", value: "购车攻略" },
        { label: "新能源车", value: "新能源车" },
        { label: "赛车", value: "赛车" },
        { label: "改装玩车", value: "改装玩车" },
        { label: "摩托车", value: "摩托车" },
        { label: "房车", value: "房车" },
        { label: "汽车生活", value: "汽车生活" },
      ],
      "运动": [
        { label: "篮球", value: "篮球" },
        { label: "足球", value: "足球" },
        { label: "健身", value: "健身" },
        { label: "竞技体育", value: "竞技体育" },
        { label: "运动文化", value: "运动文化" },
        { label: "运动综合", value: "运动综合" },
      ],
      "科技": [
        { label: "数码", value: "数码" },
        { label: "软件应用", value: "软件应用" },
        { label: "计算机技术", value: "计算机技术" },
        { label: "科工机械", value: "科工机械" },
        { label: "极客DIY", value: "极客DIY" },
      ],
      "动物圈": [
        { label: "喵星人", value: "喵星人" },
        { label: "汪星人", value: "汪星人" },
        { label: "小宠异宠", value: "小宠异宠" },
        { label: "野生动物", value: "野生动物" },
        { label: "动物二创", value: "动物二创" },
        { label: "动物综合", value: "动物综合" },
      ],
      "舞蹈": [
        { label: "宅舞", value: "宅舞" },
        { label: "舞蹈综合", value: "舞蹈综合" },
        { label: "颜值·网红舞", value: "颜值·网红舞" },
        { label: "街舞", value: "街舞" },
        { label: "明星舞蹈", value: "明星舞蹈" },
        { label: "国风舞蹈", value: "国风舞蹈" },
        { label: "舞蹈教学", value: "舞蹈教学" },
      ],
      "国产": [
        { label: "国产动画", value: "国产动画" },
        { label: "国产原创相关", value: "国产原创相关" },
        { label: "布袋戏", value: "布袋戏" },
        { label: "资讯", value: "资讯" },
        { label: "动态漫·广播剧", value: "动态漫·广播剧" },
      ],
      "鬼畜": [
        { label: "鬼畜教学", value: "鬼畜教学" },
        { label: "音MAD", value: "音MAD" },
        { label: "人力VOCALOID", value: "人力VOCALOID" },
        { label: "鬼畜剧场", value: "鬼畜剧场" },
        { label: "教程演示", value: "教程演示" },
      ],
      "纪录片": [
        { label: "人文·历史", value: "人文·历史" },
        { label: "科学·探索自然", value: "科学·探索自然" },
        { label: "军事", value: "军事" },
        { label: "社会·美食·旅行", value: "社会·美食·旅行" },
      ],
      "番剧": [
        { label: "资讯", value: "资讯" },
        { label: "官方延伸", value: "官方延伸" },
      ],
      "电视剧": [{ label: "国产剧", value: "" }],
      "电影": [{ label: "国产电影", value: "国产电影" }],
    };

    // 计算属性，基于选中的国家过滤城市
    const filteredCities = computed(() => {
      return video.subZoneKey ? cities[video.subZoneKey] : [];
    });

    // 处理国家选择变化
    const handleCountryChange = () => {
      video.subZoneValue = ""; // 重置城市选择
    };
    //删除标签
    const handleClose = (tag) => {
      const index = dynamicTags.value.indexOf(tag);
      if (index !== -1) {
        dynamicTags.value.splice(index, 1);
      }
    };

    const showInput = () => {
      inputVisible.value = true;
      nextTick(() => {
        InputRef.value.focus();
      });
    };
    //输入标签
    const handleInputConfirm = () => {
      if (inputValue.value) {
        // 检查标签是否重复
        const exists = dynamicTags.value.includes(inputValue.value);
        if (exists) {
          ElMessage({
            message: "标签已存在",
            type: "info",
            offset: 376,
          });
          return; // 直接返回，不添加标签
        }

        if (dynamicTags.value.length < MAX_TAGS) {
          // 检查当前标签数量
          dynamicTags.value.push(inputValue.value); // 将输入的标签赋值给 dynamicTags
        } else {
          ElMessage({
            message: "标签不能超过5个",
            type: "info",
            offset: 376,
          });
        }
      }
      inputVisible.value = false;
      inputValue.value = ""; // 清空输入框
    };

    const remainingCharacters = computed(() => {
      return video.content.length; // 计算剩余字数
    });
    const getImageUrl = (file) => {
      // 使用 URL.createObjectURL 创建 URL
      return file.url || file.thumbUrl || URL.createObjectURL(file.raw);
    };
    //保存视频缩略图
    const selectThumbnail = (index) => {
      thumbnail.value = null;
      selectedThumbnail.value = thumbnails.value[index]; // 保存选中的缩略图
    };
    //清除空格
    function removeSpaces() {
      inputValue.value = inputValue.value.replace(/\s+/g, "");
    }
    //清除上传后的视频数据
    function cleanVideoData() {
      video.allowTwo = false;
      video.tag = [];
      dynamicTags.value = [];
      video.content = "";
      video.title = "";
      video.subZoneKey = "";
      video.subZoneValue = "";
      const inputDom = document.querySelector(".content-input");
      inputDom.innerHTML = "";
      video.type = 0;

    }

     //获取@用户列表
    async function getEitList(){

      try {

        const response = await apiClient.get(`/user/getEit/${store.userId}`,
        {
          headers:{
            "Content-Type": "application/json",
            "Authorization": store.token,
          }
        });

        if(response.data.code===1)
          Object.assign(eitList, response.data.data);
        
      } catch (error) {
        
      }

    }

     //简介输入框
     function handleContent(){  

      const inputDom = document.querySelector(".content-input");
      if(inputDom.innerText.length>2000)
      inputDom.innerText=inputDom.innerText.slice(0,2000);
      if(inputDom.innerHTML.trim().length>0)
     { 
      video.contentHtml = inputDom.innerHTML;
      video.content = inputDom.innerText.trim();
      placeholderFlag.value=false;
    }
      else
    { 
      video.contentHtml = "";
      placeholderFlag.value=true;
    }
    
      // 获取当前选区和光标位置
      const selection = window.getSelection();
      if (!selection.rangeCount) return; // 如果没有选区，直接返回

      const range = selection.getRangeAt(0);

      // 获取光标的 X 坐标
      eitLocationX.value = range.getBoundingClientRect().x;

      // 根据光标 Y 坐标更新位置
      eitLocationY.value = range.getBoundingClientRect().y > 350
        ? 241
        : range.getBoundingClientRect().y;

      // 获取光标前的所有文本
      const textBeforeCursor =  getTextBeforeCursor(range);

      // 检测是否包含 @ 并提取关键字
      const atIndex = textBeforeCursor.lastIndexOf("@");
      if (atIndex !== -1) {
        const text = textBeforeCursor.substring(atIndex);
        if (text.length > 20 || text.includes(" ")) {
          eitFlag.value = false;
          return;
        }
        eitFlag.value = true;
        eitKeyWord.value = text.substring(1).trim();
      } else {
        eitFlag.value = false;
      }
    }
    
    // 获取光标之前的文本，支持嵌套子节点
    function getTextBeforeCursor(range) {
      let textBeforeCursor = "";
      let currentNode = range.startContainer;

      // 遍历节点，拼接光标前的文本
      while (currentNode) {
        if (currentNode.nodeType === Node.TEXT_NODE) {
          // 如果是文本节点，拼接到结果中
          textBeforeCursor = currentNode.textContent.slice(0, range.startOffset) + textBeforeCursor;
          break;
        }
        // 如果是元素节点，继续向上遍历
        currentNode = currentNode.previousSibling || currentNode.parentNode;
      }

      return textBeforeCursor;
    }

    //添加@用户
    function insertEit() {

        // 获取当前选区和光标位置
        const selection = window.getSelection();
        const range = selection.getRangeAt(0);

        // 找到最近的 `@` 并删除关键字
        const containerText = range.startContainer.textContent;
        const startOffset = range.startOffset;
        const atIndex = containerText.lastIndexOf("@", startOffset - 1);

        if (atIndex !== -1) {
          // 构建新内容，删除 @关键字
          const beforeAt = containerText.slice(0, atIndex); // `@` 之前的内容
          const afterAt = containerText.slice(startOffset); // 光标之后的内容
          range.startContainer.textContent = beforeAt + afterAt;

          // 更新 Range 位置
          const newOffset = atIndex; // 新光标位置在 `@` 之前
          range.setStart(range.startContainer, newOffset);
          range.setEnd(range.startContainer, newOffset);
        }

        // 创建新的按钮元素
        const button = `<a m1a,s,a2href="./home?homeMenu=1&userId=${store.eitUserId}"  contentEditable="false" class="at-msg" style="color:#008ac5;">@${store.eitUserName}&nbsp;</a>`;
        // 插入新按钮到光标位置
        const fragment = range.createContextualFragment(button); // 创建 DOM 片段
        const lastChild = fragment.lastChild;
        range.insertNode(lastChild); // 插入按钮

        // 设置光标位置到按钮后面
        const newRange = document.createRange();
        newRange.setStartAfter(lastChild);
        newRange.setEndAfter(lastChild);
        selection.removeAllRanges();
        selection.addRange(newRange);

        // 清空输入框
        if(!eitUserNames.value.includes(store.eitUserName))
          eitUserNames.value.push(store.eitUserName);
        eitKeyWord.value = "";
        eitFlag.value = false;
        store.setEitUserName("");
        handleContent();
    }
     
    watch(()=>storer.eitUserName,()=>{

      if(storer.eitUserName!== "")
      insertEit();

     })


    //转换成html
    async function publishComment() {


      // 网址超链接
    video.contentHtml = video.contentHtml.replace(
        /(https?:\/\/[^\s<>"]+?)(?=\s|&nbsp;|<div>|<\/div>|$)\b/g,
        '<a href="$1" target="_blank"><img style="width: 14px;height: 14px;padding-right: 2px;" src="../img/网页链接.png">$1<br></a>'
    );
    // @样式
    video.contentHtml = video.contentHtml.replace(/color:#008ac5;/g, "color:#008ac5;cursor:pointer;");
    
    // 替换时间样式
    video.contentHtml = video.contentHtml.replace(
      /(?<!<span[^>]*?>)\b\d{1,2}[：:]\d{2}(:\d{2})?(?!<\/span>)/g,
        `<span class="time-span" style="color:#008ac5;cursor:pointer;" data-time="$&">$&</span>`
    );

    video.contentHtml = video.contentHtml.replace("m1a,s,a2","")
    

    // 替换本站链接格式
    const container = document.createElement("div");
    container.innerHTML = video.contentHtml; // 使用 DOM 操作管理 HTML
    
    const links = container.querySelectorAll("a");
    for (const link of links) {
        const match = link.href.match(/BV([0-9]+)/); // 检查链接是否匹配 BV 格式
        if (match) {
        try {
            const res = await apiClient.get(`/video/getVideoTitle/${match[1]}`);
            if (res.data.code === 1) {
            // 动态替换 HTML 中的链接
            link.innerHTML = `<img style="width: 14px;height: 14px;padding-right: 2px;transform: translateY(1px);" src="../img/视频标题.png">${res.data.data}<br>`;
            }
        } catch (error) {
        }
        }
    }
    video.contentHtml = container.innerHTML.replace(/(<div>\s*<br\s*\/?>\s*<\/div>\s*)+$/, ''); // 更新最终 HTML
    }
   

     

    return {
      beforeUploadVideo,
      centerDialogVisible,
      video,
      beforeUploadCover,
      handleChangeCover,
      handleRemoveCover,
      handleChangeVideo,
      handleRemoveVideo,
      handleUploadClick,
      uploadRef,
      uploadRef2,
      removeFileVideo,
      removeFileCover,
      fileListVideo,
      fileListCover,
      uploadFile,
      countries,
      handleCountryChange,
      filteredCities,
      selectedCity,
      selectedCountry,
      dynamicTags,
      inputVisible,
      InputRef,
      handleClose,
      showInput,
      inputValue,
      handleInputConfirm,
      remainingCharacters,
      fileVideoname,
      progress,
      handleUploadClickVideo,
      reVideo,
      formatFileSize,
      videoSize,
      thumbnail,
      coverImg,
      getImageUrl,
      fileNameWithoutExtension,
      thumbnails,
      selectThumbnail,
      selectedThumbnail,
      handleCut,
      defaultCover,
      removeSpaces,
      dialogVisible,
      confirmLeave,
      cancelLeave,
      beforeRouteLeave,
      loading,
      store,
      content,
      eitList,
      handleContent,
      placeholderFlag,
      eitLocationX,
      eitLocationY,
      eitFlag,
      eitKeyWord,
    };
  },
  beforeRouteLeave(to, from, next) {
    this.beforeRouteLeave(to, from, next); // 调用 setup 中的导航守卫
  },
};
</script>

<style>
.main-content4 {
  background-color: #f8f8f8;
  border-radius: 5px;
  transform: translate(-100px, -20px);
  width: 1300px;
  height: 1500px !important;
  overflow-x: hidden;
  overflow-y: hidden;
}
.white {
  transform: translate(120px, 20px);
  width: 1200px;
  height: 2000px !important;
  z-index: 10;
  background-color: white;
  overflow-x: hidden;
}

h1 {
  color: #333;
}

button {
  padding: 10px 15px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

button:hover {
  background-color: #0056b3;
}

.cover {
  width: 90px;
  position: absolute;
  height: 20px;
  color: rgb(208, 208, 208);
  font-size: 14px;
  margin-left: 120px;
  transform: translate(55px, 0px);
  cursor: pointer;
  z-index: 10;
}

.el-dialog__title {
  font-size: 16px;
  color: #00a1d6; /* 文字颜色 */
}
.uploadCoverImg {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #00a1d6;
  width: 120px;
  border-radius: 5px;
  height: 30px;
  transition: background-color 0.3s; /* Smooth transition */
  transform: translate(267px, 50px);
  font-size: 14px;
  cursor: pointer;
}

.uploadCoverImg:hover {
  background-color: #65b6df;
}

.commit {
  transform: translate(430px, -85px);
  position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #00a1d6;
  width: 120px;
  border-radius: 5px;
  height: 40px;
  cursor: pointer;
}

.commit:hover {
  background-color: #65b6df;
}
.crop {
  background-color: white;
  width: 120px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  transform: translate(170px, 175px);
  border-radius: 5px;
  color: black;
  border: 1px solid #aaaaaa;
  cursor: pointer;
}
.crop:hover {
  background-color: #cfcfcf;
}
.thumbnail-slider {
  overflow-x: auto;
  white-space: nowrap;
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 5px;
  transform: translate(20px, -20px);
  width: 620px;
  height: 100px;
}

.thumbnails {
  display: flex;
}

.thumbnail {
  width: 100px;
  height: auto;
  margin-right: 10px;
  cursor: pointer;
}
.el-dialog .el-dialog__headerbtn:hover {
  background-color: white;
}
.m-2:hover {
  color: #00a1d6 !important;
  background-color: white;
}
.forbind:hover {
  color: #00a1d6 !important;
}

.el-popover {
  font-size: 12px !important;
  color: black;
  background-color: #fcfae2 !important;
  border: 1px solid #f3d2a0 !important;
}

.forter2 a:hover {
  opacity: 0.7;
}

.content-input{
  position: relative;
  margin-top: 20px;
  width: 746px;
  height: 160px;
  border-radius: 10px;
  border: 1px solid #ccc;
  padding: 10px 80px 10px 10px;
  font-size: 14px;
  transform: translate(112px,-38px);
  overflow: hidden;
  overflow-y: auto;
}

.content-input:focus{
  outline: none;
}

.placeholder {
  position: absolute;
  transform: translate(95px,13.5px);
  color: #ccc;
  font-size: 14px;
  pointer-events: none;
}

.content-length{
  position: absolute;
  transform: translate(850px,150px);
  color: #ccc;
  font-size: 12px;
  pointer-events: none;
}

.eit-container{
  position: relative;
  left: 135px;
  top: -80px;
}

a{
  text-decoration: none;
  color: #00aeec;
}



</style>
