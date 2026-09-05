import axios from "axios"

export const api = axios.create({
    baseURL: "http://localhost:9090/api/v1",
   // withCredentials: true,  // for authenticated requests
})
