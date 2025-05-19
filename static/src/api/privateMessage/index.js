import apiClient from "../../services/apiClient";

//获取私信对话列表
export async function selectDialogue(token,userId,pageNum){

    const res = await apiClient.get(`/privateMessage/selectDialogue`,
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

//修改置顶状态
export async function changeUpStatus(store,dialogueId){

    const res=await apiClient.put("/privateMessage/putUpStatus",{},
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": store.token,
        },
        params:{
            userId: store.userId,
            id: dialogueId,
        }}
    );

    return res;

}

//修改免打扰状态
export async function changeDndStatus(store,dialogueId){

    const res=await apiClient.put("/privateMessage/putDndStatus",{},
        {headers: {
              "Content-Type": "application/json",
              "Authorization": store.token,
        },
        params:{
            userId: store.userId,
            id: dialogueId,
        }}
    );

    return res;

}

//新增私信对话
export async function addDialogue(token,dialogue){
    const res = await apiClient.post("/privateMessage/addDialogue",dialogue,
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": token,
        },
    });

    return res;
}

//删除私信对话
export async function deleteDialogue(token,id){
    const res = await apiClient.delete(`/privateMessage/deleteDialogue`,
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": token,
        },
        params:{
            id,
        }
    });

    return res;
}

//发送消息
export async function sendMessage(token,message){

    const res = await apiClient.post("/privateMessage/sendMessage",message,
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": token,
        },
    });

    return res;
}

//撤回消息
export async function revocationMessage(store,id){

    const res = await apiClient.delete(`/privateMessage/revocationMessage`,
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": store.token,
        },
        params:{
            id,
            userId: store.userId,
        }
    });

    return res;
}

//修改消息状态
export async function changeMessageStatus(store,dialogueId){

    const res = await apiClient.put("/privateMessage/putMessageStatus",{},
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": store.token,
        },
        params:{
            dialogueId,
            userId: store.userId,
        }
    });

    return res;
}

//删除私信
export async function deletePrivateMessage(store,id){

    const res = await apiClient.delete(`/privateMessage/deleteMessage`,
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": store.token,
        },
        params:{
            id,
            userId: store.userId,
        }
    });

    return res;
}

//查询私信
export async function selectPrivateMessage(store,dialogueId,pageNum){
    const res = await apiClient.get(`/privateMessage/selectMessage`,
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": store.token,
        },
        params:{
            dialogueId,
            userId: store.userId,
            pageNum,
        }
    });

    return res;
}

//修改用户的回复、@、被点赞的数量
export async function putMessageNumber(store){

    const res = await apiClient.put("/privateMessage/putMessageNumber",{},
    {
        headers: {
              "Content-Type": "application/json",
              "Authorization": store.token,
              },
        params:{
            messageMenu: store.messageMenu,
            userId: store.userId,
        }
    });
    return res;
}

//分享视频
export async function shareVideoAxios(token,shareVideo){
    const res= await apiClient.post("/privateMessage/shareVideo",shareVideo,
    {
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
            },
    }
    )
    return res;
}