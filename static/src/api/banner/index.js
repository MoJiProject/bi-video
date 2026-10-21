import { del, get, post, put } from "../../services/http";

export const getHomeBanners = () => get("/banner");
export const searchBanners = (params) => get("/system/banner", params);
export const saveBanner = (id, data) => id
  ? put(`/system/banner/${id}`, data)
  : post("/system/banner", data);
export const deleteBanner = (id) => del(`/system/banner/${id}`);

export function uploadBannerImage(file) {
  const data = new FormData();
  data.append("file", file);
  return post("/system/banner/image", data);
}
