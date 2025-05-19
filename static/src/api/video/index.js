import apiClient from "../../services/apiClient";

//更新稍后观看状态
export async function updateWaitWatch(token,dynamicDto){
    const response = await apiClient.put(
        "/dynamic/updateWaitWatch",dynamicDto,
          {
          headers: {
          "Content-Type": "application/json",
          "Authorization": token,
        },        
      }
    );
    return response;    
}

//查询主页视频
export async function selectVideo(userId,pageNum,sort){
    const response = await apiClient.get(
        "/video/selectVideo",
        {
          headers: {
            "Content-Type": "application/json",
          },
          params: {
            pageNum,
            userId,
            sort,
          }
        }
      );
    return response;
}
