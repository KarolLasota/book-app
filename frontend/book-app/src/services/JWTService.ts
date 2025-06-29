import { jwtDecode } from "jwt-decode";



export class JWTService {



  public static isTokenExpired(token: string): boolean {
    if (!token) return true;
  
    try {
      const decoded = jwtDecode(token) as { exp: number };
      if (!decoded.exp) return true;
  
      const expiryTime = decoded.exp * 1000;
      const serverTimeOffset = 2 * 60 * 60 * 1000; 
  
      return Date.now() > (expiryTime - serverTimeOffset);
    } catch {
      return true;
    }
  }


  public static decodeJwt<T>(token: string): T {
    return (jwtDecode as any)(token);
  }


}