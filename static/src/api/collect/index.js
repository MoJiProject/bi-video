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

//查询待看清单的视频
export function selectWaitWatch(params, config = {}) {
    return apiClient.get('/watch-later/list', { ...config, params });
}

//清除所有待看清单
export function cleanAllWaitWatch() {
    return apiClient.delete('/watch-later');
}

export function deleteWaitWatch(videoIds) {
    return apiClient.delete('/watch-later/batch', { data: { videoIds } });
}