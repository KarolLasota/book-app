import axios from "axios";
import { AuthResponse } from "../types/Auth";
import axiosInstance from "./axiosInstance";

const API = "http://localhost:8080/auth"; 

export const login = (email: string, password: string) =>
  axiosInstance.post<AuthResponse>(`${API}/login`, { email, password });

export const register = (email: string, password: string) =>
  axiosInstance.post<AuthResponse>(`${API}/register`, { email, password });

export const refreshToken = () =>
  axiosInstance.post<AuthResponse>(`${API}/refresh`, {});

export const logout = () =>
  axios.post(`${API}/logout`, {}, { withCredentials: true });
