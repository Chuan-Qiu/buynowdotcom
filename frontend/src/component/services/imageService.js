import { api } from "./api";

/**
 * The backend serves image bytes at /images/{imageId}/download. That path is
 * built here and nowhere else — components pass an id, never a URL — so the
 * route lives in exactly one place and goes through the shared Axios instance.
 *
 * Note we do not consume the `downloadUrl` field on ImageDto directly: it
 * already contains the `/api/v1` prefix that `api` supplies via its baseURL,
 * so passing it straight to `api.get` would double the prefix.
 */
export const fetchProductImage = async (imageId) => {
  const response = await api.get(`/images/${imageId}/download`, {
    responseType: "blob",
  });
  return URL.createObjectURL(response.data);
};
