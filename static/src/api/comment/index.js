import apiClient from "../../services/apiClient";

//获取私信对话列表
export async function selectReplayComment(token,userId,pageNum){

    const res = await apiClient.get(`/comment/selectReplayComment`,
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": token,
        },
        params:{
            userId,
            pageNum
        }
    });
    return res;

}

//操作评论（点赞、踩）
export async function commentControles(token,commentControls){
    const res = await apiClient.post(
        "/comment/controlsComment",
        commentControls,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: token,
          },
        },
      );
    return res;
}

//删除回复通知
export async function deleteCommentNotification(token,commentId,userId,messageMenu,type){

    const res = await apiClient.delete("/comment/deleteCommentNotification",
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": token,
        },
        params:{
            commentId,
            userId,
            messageMenu,
            type  
        }
    });
    return res;
}

//回复评论
export async function replayComment(token,comment){

    const res = await apiClient.post("/comment/addComment", comment, {
        headers: {
          "Content-Type": "application/json",
          Authorization: token,
        },
      });
    return res;
}

//获取at用户的评论
export async function selectAtComment(token,userId,pageNum){

  const res = await apiClient.get("/comment/selectAt",
  {
      headers: {
            "Content-Type": "application/json",
            "Authorization": token,
      },
      params:{
          userId,
          pageNum
      }
  });
  return res;
}

//查询喜欢用户的视频或评论
export async function selectLike(token,userId,pageNum){

  const res = await apiClient.get("/comment/selectLike",
  {
      headers: {
            "Content-Type": "application/json",
            "Authorization": token,
      },
      params:{
          userId,
          pageNum
      }
  });
  return res;

}

//更改点赞消息提示状态
export async function dndLikeNotification(token,userId,id,type){

  const res = await apiClient.put("/comment/dndLikeNotification",{},
  {
      headers: {
            "Content-Type": "application/json",
            "Authorization": token,
      },
      params:{
          userId,
          id,
          type
      }
  });
  return res;

}

//查询喜欢评论或视频的用户
export async function selectLikeUser(token,userId,id,pageNum,type){

  const res = await apiClient.get("/comment/selectLikeUser",
  {
      headers: {
            "Content-Type": "application/json",
            "Authorization": token,
      },
      params:{
          userId,
          id,
          pageNum,
          type
      }
  });
  return res;

}

//修改评论置顶状态
export async function putUpStatus(token,userId,id){

  const res = await apiClient.put("/comment/putUpStatus",{},
  {
      headers: {
            "Content-Type": "application/json",
            "Authorization": token,
      },
      params:{
          userId,
          id
      }
  });
  return res;

}

//查询单个评论
export async function selectOneComment(token,userId,mainCommentId,replyCommentId){
  const res = await apiClient.get("/comment/selectOneComment",
  {
      headers: {
            "Content-Type": "application/json",
            "Authorization": token,
      },
      params:{
          userId,
          mainCommentId,
          replyCommentId
      }
  });
  return res;
}