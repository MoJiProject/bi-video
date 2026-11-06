import apiClient from "../../services/apiClient";

//关注用户
export async function addFollow(token,userId,upUserId){

    const response = await apiClient.put(
        `/user/${upUserId}/follow`,
        null,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: token,
          },
        },
      );
      return response;    
}

//取消关注
export async function deleteFollow(token,userId,upUserId){
    const response = await apiClient.delete(
        `/user/${upUserId}/follow`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: token,
          },
        },
      );
      return response;    
}

//修改消息提醒设置
export async function putMessageSettings(store){
    const response = await apiClient.put(
        "/user/putMessageSettings",
          {
            messageWarn: store.userInformation.messageWarn,
            replyMessageWarn: store.userInformation.replyMessageWarn,
            atMessageWarn: store.userInformation.atMessageWarn,
            likeMessageWarn: store.userInformation.likeMessageWarn,
          },
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: store.token, 
          },
        },
      );
    return response;    
}

//修改账号资料
export async function updateAccount(token,account){
    const response = await apiClient.put(
        "/auth/account",
        account,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: token, 
          },
        },
      );
    return response;    
}

async function toImageFile(image, name) {
  if (image instanceof File) return image;

  const response = await fetch(image);
  if (!response.ok) throw new Error("图片读取失败");
  const blob = await response.blob();
  const extension = blob.type === "image/png" ? "png" : blob.type === "image/gif" ? "gif" : "jpg";
  return new File([blob], `${name}.${extension}`, { type: blob.type || "image/jpeg" });
}

async function uploadMedia(token, category, image) {
  const file = await toImageFile(image, category);
  const body = new FormData();
  body.append("file", file);
  return apiClient.post(`/media/${category}`, body, {
    headers: { Authorization: token },
  });
}

//上传头像
export async function uploadAvatar(token,avatar) {
  return uploadMedia(token, "avatar", avatar);
}

//上传背景
export async function uploadBackground(token,background) {
  return uploadMedia(token, "background", background);
}
