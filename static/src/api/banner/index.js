import apiClient from "../../services/apiClient";

export function getHomeBanners() {
  return apiClient.get("/banner");
}

export function searchBanners(token, params) {
  return apiClient.get("/system/banner", { headers: { Authorization: token }, params });
}

export function saveBanner(token, id, data) {
  const config = { headers: { Authorization: token } };
  return id
    ? apiClient.put(`/system/banner/${id}`, data, config)
    : apiClient.post("/system/banner", data, config);
}

export function deleteBanner(token, id, operatorId) {
  return apiClient.delete(`/system/banner/${id}`, {
    headers: { Authorization: token },
    params: { operatorId },
  });
}

export function uploadBannerImage(token, operatorId, file) {
  const data = new FormData();
  data.append("file", file);
  return apiClient.post("/system/banner/image", data, {
    headers: { Authorization: token },
    params: { operatorId },
  });
}
