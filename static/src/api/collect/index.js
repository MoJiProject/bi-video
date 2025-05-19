import apiClient from "../../services/apiClient";
import qs from "qs";

//删除失效收藏的视频
export async function deleteFailure(token,userId){
    const res = await apiClient.delete("/collect/deleteFailure", {
        headers: {
          "Content-Type": "application/json",
          Authorization: token,
        },
        params: {
            userId,
        },
    });
    return res;

}

//取消收藏
export async function deleteCollect(token,userId,ids){

    const res = await apiClient.delete("/collect/deleteCollect", {
        headers: {
          "Content-Type": "application/json",
          Authorization: token, 
        },
        params: {
            userId,
            ids,
        },
        paramsSerializer: params => qs.stringify(params, {
            arrayFormat: 'repeat'
          }),
    });
    return res;
}

//复制或移动收藏的视频
export async function controlCollect(token,userId,collectIds,collectClassifyIds,controls){
    const res = await apiClient.put("/collect/controlCollect",{}, {
        headers: {
          "Content-Type": "application/json",
          Authorization: token,
        },
        params: {
            userId,
            collectIds,
            collectClassifyIds,
            controls,
        },
        paramsSerializer: params => qs.stringify(params, {
            arrayFormat: 'repeat'
          }),
    });
    return res;
}

//查询稍后再看的视频
export async function selectWaitWatch(token,userId,pageNum,sort,sort2,keyWord,startTime,endTime){

    const res = await apiClient.get("/collect/selectWaitWatch", {
        headers: {
          "Content-Type": "application/json",
          Authorization: token,
        },
        params: {
            userId,
            pageNum,
            sort,
            sort2,
            keyWord,
            startTime,
            endTime,
        },
    });
    return res;
}

//清除所有稍后再看
export async function cleanAllWaitWatch(token,userId){

    const res = await apiClient.delete("/collect/cleanAllWaitWatch", {
        headers: {
          "Content-Type": "application/json",
          Authorization: token,
        },
        params: {
            userId,
        },
    });
    return res;
}