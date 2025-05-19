import apiClient from "../../services/apiClient";

//新增收藏夹
export async function addCollectClassify(token,collectClassify){
    const res = await apiClient.post("/collectClassify/addCollectClassify", collectClassify, {
        headers: {
          "Content-Type": "application/json",
          Authorization: token,
        },
    });
    return res;
}

//删除收藏夹
export async function deleteCollectClassify(token,userId,id){
    const res = await apiClient.delete("/collectClassify/deleteCollectClassify", {
        headers: {
          "Content-Type": "application/json",
          Authorization: token,
        },
        params: {
            id,
            userId,
        },
    });
    return res;
}

//修改收藏夹
export async function updateCollectClassify(token,collectClassify){
    const res = await apiClient.put("/collectClassify/putCollectClassify", collectClassify, {
        headers: {
          "Content-Type": "application/json",
          Authorization: token,
        },
    });
    return res;
}

