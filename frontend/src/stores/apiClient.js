import axios from 'axios';
import {useAuthStore} from "@/stores/auth.js";

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api/v1',
    withCredentials: true,
});

const refreshAccessToken = async () => {
    const store = useAuthStore();
    try {
        const response = await apiClient.post('/user/refresh-token', null, {
            headers: {
                'Authorization-Refresh': `Bearer ${store.refreshToken}`
            }
        });

        store.accessToken = response.headers['accessToken'];
        store.refreshToken = response.headers['refreshToken'];
    } catch (error) {
        console.error('Failed to refresh access token:', error);
        store.logout();
        window.location.href = 'http://localhost:5173';
    }
};

// 공통 요청 함수
const request = async (method, endpoint, data = null) => {
    const store = useAuthStore();
    try {
        const response = await apiClient.request({
            method: method,
            url: endpoint,
            headers: {
                Authorization: `Bearer ${store.accessToken}`
            },
            data: data
        });
        return response;
    } catch (error) {
        if (error.response && error.response.status === 401) {
            await refreshAccessToken();

            const retryResponse = await apiClient.request({
                method: method,
                url: endpoint,
                headers: {
                    Authorization: `Bearer ${store.accessToken}`,
                },
                data: data
            });
            return retryResponse;
        } else {
            throw error;
        }
    }
};

const getFetch = (endpoint) => request('GET', endpoint);
const postFetch = (endpoint, data) => request('POST', endpoint, data);
const putFetch = (endpoint, data) => request('PUT', endpoint, data);
const delFetch = (endpoint) => request('DELETE', endpoint);

export { getFetch, postFetch, putFetch, delFetch };
