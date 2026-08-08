import { useQuery } from '@tanstack/react-query'
import { authApi } from '../lib/api'

export function useAuth() {
  const { data } = useQuery({ queryKey: ['authMe'], queryFn: authApi.me, staleTime: 60_000 })
  const esAdmin = data?.authorities.includes('ROLE_ADMIN') ?? false
  const username = data?.username ?? ''
  return { esAdmin, username, authorities: data?.authorities ?? [] }
}
