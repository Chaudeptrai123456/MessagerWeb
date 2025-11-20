from fastapi import FastAPI, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import jwt, JWTError
import httpx

app = FastAPI()

# JWKS URI giống Spring
JWKS_URL = "http://localhost:9999/oauth2/jwks"
ALGORITHM = "RS256"

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

# Cache JWKS
jwks_cache = None

async def get_jwks():
    global jwks_cache
    if jwks_cache is None:
        async with httpx.AsyncClient() as client:
            resp = await client.get(JWKS_URL)
            resp.raise_for_status()
            jwks_cache = resp.json()
    return jwks_cache

def get_public_key(jwks, kid):
    for key in jwks["keys"]:
        if key["kid"] == kid:
            return jwt.algorithms.RSAAlgorithm.from_jwk(key)
    raise Exception("Public key not found")

async def verify_token(token: str = Depends(oauth2_scheme)):
    try:
        jwks = await get_jwks()
        headers = jwt.get_unverified_header(token)
        kid = headers["kid"]
        public_key = get_public_key(jwks, kid)

        payload = jwt.decode(token, public_key, algorithms=[ALGORITHM])
        return payload
    except JWTError as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Token không hợp lệ: {str(e)}"
        )
