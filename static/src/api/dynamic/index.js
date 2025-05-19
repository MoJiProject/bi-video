import apiClient from "../../services/apiClient";

//修改动态置顶状态
export async function putUpStatus(dynamicId,userId,token){

    const response = await apiClient.put("/dynamic/putUpStatus",{},{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        },
        params: {
            userId,
            dynamicId,
        }
    });
    return response;    
}

//点赞动态
export async function likeDynamic(dynamicId,userId,token){
    
    const response = await apiClient.put("/dynamic/likeDynamic",{},{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        },
        params: {
            userId,
            dynamicId,
        }
    });
    return response;    
}

//删除动态
export async function deleteDynamic(dynamicId,userId,token){
    const response = await apiClient.delete("/dynamic/deleteDynamic",{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        },
        params: {
            userId,
            dynamicId,
        }
    });
    return response;    
}

//新增动态
export async function addDynamic(token,dynamic){

    const response = await apiClient.post("/dynamic/addDynamic",dynamic,{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        }
    });
    return response;    
}

//清除动态提醒
export async function cleanDynamicMessage(token,userId,dynamicUserId){

    const response = await apiClient.delete("/dynamic/cleanDynamicMessage",{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        },
        params: {
            userId,
            dynamicUserId,
        }
    });
    return response;    
}

//查询关注的动态用户
export async function usersDynamic(token,userId,pageNum){

    const response = await apiClient.get("/dynamic/usersDynamic",{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        },
        params: {
            userId,
            pageNum,
        }
    });
    return response;    
}

//查询用户主页的动态(动态页)
export async function getDynamic(token,userId,dynamicUserId,pageNum,type){

    const response = await apiClient.get("/dynamic/getDynamic",{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        },
        params: {
            userId,
            dynamicUserId,
            type,
            pageNum,
        }
    });
    return response;    
}

//查询是否有新动态
export async function allDynamicMessage(token,userId){
    const response = await apiClient.get("/dynamic/allDynamicMessage",{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        },
        params: {
            userId,
        }
    });
    return response;    
}

//根据动态id查询动态
export async function selectDynamicById(dynamicId){

    const response = await apiClient.get("/dynamic/selectDynamicById",{
        headers: {
            "Content-Type": "application/json",
        },
        params: {
            dynamicId,
        }
    });
    return response;    
}

//根据动态id查询动态用户是否点赞
export async function selectDynamicByIdLike(token,dynamicId,userId){
    const response = await apiClient.get("/dynamic/selectDynamicByIdLike",{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        },
        params: {
            dynamicId,
            userId,
        }
    });
    return response;    
}

//查询喜欢动态的用户
export async function likeDynamicUser(userId,dynamicId,pageNum){
    const response = await apiClient.get("/dynamic/likeDynamicUser",{
        headers: {
            "Content-Type": "application/json",
        },
        params: {
            userId,
            dynamicId,
            pageNum,
        }
    });
    return response;    
}