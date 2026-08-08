import axios from 'axios'

export const api = axios.create({
  baseURL: '/api',
})

export interface ApiError {
  message?: string
  detail?: string
}
