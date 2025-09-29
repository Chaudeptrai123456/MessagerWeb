from fastapi import Request
from fastapi.responses import RedirectResponse
from auth_client import AuthClient
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.responses import JSONResponse
import jwt  as PyJWT # PyJWT
import requests

# Config Auth Client
AUTH_SERVER_URL = "http://localhost:9999"
CLIENT_ID = "chau"
CLIENT_SECRET = "123"
REDIRECT_URI = "http://localhost:9999/login/oauth2/code/messenger"  # phải khớp với redirectUri trong Spring config

auth_client = AuthClient(AUTH_SERVER_URL, CLIENT_ID, CLIENT_SECRET)

class AuthMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        # Các route public
        if request.url.path in ["/", "/login", "/callback"]:
            return await call_next(request)

        # Kiểm tra header Authorization
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            # Redirect đến Authorization Server để login
            return RedirectResponse(
                url=f"{AUTH_SERVER_URL}/oauth2/authorize?response_type=code&client_id={CLIENT_ID}&redirect_uri={REDIRECT_URI}&scope=openid profile email"
            )

        # Nếu có token thì kiểm tra hợp lệ
        token = auth_header.split(" ")[1]
        try:
            # Lấy public key từ Authorization Server để verify JWT
            jwks = requests.get(f"{AUTH_SERVER_URL}/oauth2/jwks", timeout=5).json()
            public_key = jwt.algorithms.RSAAlgorithm.from_jwk(jwks["keys"][0])
            decoded = jwt.decode(token, public_key, algorithms=["RS256"], audience=CLIENT_ID)
            request.state.user = decoded  # lưu thông tin user vào request
        except Exception as e:
            return JSONResponse(status_code=401, content={"error": "Invalid token", "details": str(e)})

        return await call_next(request)
