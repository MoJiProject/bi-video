import apiClient from "../../services/apiClient";
import qs from "qs";


//清空历史记录
export async function deleteAllHistory(userId,token){
    const response = await apiClient.delete("/history/deleteAllHistory",{
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

//查询历史记录
export async function selectHistoryList(token,userId,pageNum,sort,startTime,endTime,keyWord){
    const response = await apiClient.get("/history/selectHistoryList",{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        },
        params: {
            userId,
            pageNum,
            sort,
            startTime,
            endTime,
            keyWord,
        }
    });
    return response;    
}

//批量删除历史记录
export async function deleteHistory(token,userId,ids){
    const response = await apiClient.delete("/history/deleteHistory",{
        headers: {
            "Content-Type": "application/json",
            "Authorization": token,
        },
        params: {
            userId,
            ids,
        },
        paramsSerializer: params => qs.stringify(params, {
            arrayFormat: 'repeat'
          }),
    });
    return response;    
}