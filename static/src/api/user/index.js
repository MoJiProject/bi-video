import apiClient from "../../services/apiClient";

//检查登录
export async function ChecklLogin(userIp) {
   
const response = await apiClient.get(`/user/checkLoginFlag/${userIp}`,
);
return response;
}

//获取用户ip和token
export async function getUserIp(){

const response = await apiClient.get("/userIp/getUserIp");
return response;

}

//关注用户
export async function addFollow(token,userId,upUserId){

    const response = await apiClient.post(
        "/video/addFollow",
        {
          followId: upUserId,
          fansId: userId,
        },
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
    const response = await apiClient.post(
        "/video/deleteFollow",
        {
          followId: upUserId,
          fansId: userId,
        },
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
          store.userInformation,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: store.token, 
          },
        },
      );
    return response;    
}

//查询互相关注列表
export async function selectFollowAndFans(store,pageNum){
    const response = await apiClient.get(
        `/user/selectFollowAndFans`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: store.token, 
          },
          params: {
            userId: store.userId,
            pageNum,
          }
        },
      );
    return response;    
}

//查询用户信息
export async function selectUserInfo(id){
    const response = await apiClient.get(`/user/userInfo/${id}`);
    return response;    
}

//修改用户信息
export async function putUserInfo(token,userInfo2){
    const response = await apiClient.put(
        "/user/putUserInfo",
        userInfo2,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: token, 
          },
        },
      );
    return response;    
}

//修改主页设置
export async function putHomeSettings(token,userInfo2){
  const response = await apiClient.put(
      "/user/putSettings",
      userInfo2,
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: token, 
        },
      },
    );
  return response;    
}

//查询是否关注该用户
export async function checkFollow(userId,viewUserId,token){

    const response = await apiClient.get(
        "/user/selectFollow",
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: token, 
          },
          params: {
            userId,
            viewUserId,
          }
        },
      );
    return response;    
}

//查询主页的关注列表或粉丝列表
export async function fansOrFollow(homeUserId,userId,type,pageNum,keyWord,token){

  const response = await apiClient.get("/user/fansOrFollow",{
      headers: {
          "Content-Type": "application/json",
          Authorization: token, 
      },
      params: {
          homeUserId,
          userId,
          type,
          pageNum,
          keyWord,
      }
  });
  return response;    
}

//清除新粉丝提醒
export async function putNewFans(token,userId){

  await apiClient.put(
      "/user/putNewFans",{},
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: token, 
        },
         params: {
           userId,
         }
      }
    );
}

 //获取@用户列表
export async function getEitList(store) {
    const response = await apiClient.get(`/user/getEit/${store.userId}`, {
      headers: {
        "Content-Type": "application/json",
        Authorization: store.token,
      },
    });
    return response;
}

//上传头像
export async function uploadAvatar(token,userId,avatar) {

  const response = await apiClient.post("/user/uploadAvatar",avatar,{
      headers: {
          "Content-Type": "application/json",
          Authorization: token,
      },
      params: {
          userId,
      },
  });
  return response;
}

//上传背景
export async function uploadBackground(token,userId,background) {

  const response = await apiClient.post("/user/uploadBackground",background,{
      headers: {
          "Content-Type": "application/json",
          Authorization: token,
      },
      params: {
          userId,
      },
  });
  return response;
}
