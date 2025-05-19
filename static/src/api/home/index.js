import apiClient from "../../services/apiClient";

//根据用户id查询主页投稿的视频
export async function homeContributeVideos(homeUserId,userId,sort,pageNum,keyWord){
    const response = await apiClient.get("/home/contribute",{
        headers: {
            "Content-Type": "application/json",
        },
        params: {
            userId,
            homeUserId,
            sort,
            pageNum,
            keyWord,
        }
    });
    return response;    
}

//根据用户id查询最近投币的视频
export async function homeThrowCoinVideos(userId,homeUserId){
    const response = await apiClient.get("/home/throwCoin",{
        headers: {
            "Content-Type": "application/json",
        },
        params: {
            userId,
            homeUserId,
        }
    });
    return response;    
}

//根据用户id查询最近点赞的视频
export async function homeLoveVideos(userId,homeUserId){
    const response = await apiClient.get("/home/love",{
        headers: {
            "Content-Type": "application/json",
        },
        params: {
            userId,
            homeUserId,
        }
    });
    return response;    
}

//根据用户id查询主页的动态图片
export async function dynamicImg(homeUserId,type,pageNum){
    const response = await apiClient.get("/home/dynamicImg",{
        headers: {
            "Content-Type": "application/json",
        },
        params: {
            homeUserId,
            type,
            pageNum,
        }
    });
    return response;    
}

//查询用户主页的动态
export async function homeDynamic(homeUserId,userId,pageNum,type,keyWord){

    const response = await apiClient.get("/home/dynamic",{
        headers: {
            "Content-Type": "application/json",
        },
        params: {
            homeUserId,
            userId,
            pageNum,
            type,
            keyWord
        }
    });
    return response;    
}

//查询代表作
export async function homeMagnumVideos(homeUserId,userId){
    const response = await apiClient.get("/home/magnum",{
        headers: {
            "Content-Type": "application/json",
        },
        params: {
            homeUserId,
            userId,
        }
    });
    return response;    
}

//获取用户所有收藏夹
export async function getCollectsClassify(homeUserId,userId,token){

    const response = await apiClient.get("/home/collectsClassify",{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token
        },
        params: {
            homeUserId,
            userId,
        }
    });
    return response;    
}

//根据收藏夹名称获取收藏
export async function getCollectByName(collectName,homeUserId,userId,pageNum,type,sort,keyWord){

    const response = await apiClient.get("/home/collectByName",{
        headers: {
            "Content-Type": "application/json",
        },
        params: {
            collectName,
            homeUserId,
            userId,
            pageNum,
            type,
            sort,
            keyWord,
        }
    });
    return response;    
}