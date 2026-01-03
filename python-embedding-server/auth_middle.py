from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import jwt, JWTError
from jose.backends import RSAKey
import httpx
import time

JWKS_URL = "http://localhost:9999/oauth2/jwks"
ALGORITHM = "RS256"

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

jwks_cache = None
jwks_cached_at = 0
JWKS_TTL = 300  # 5 phút


async def get_jwks():
    global jwks_cache, jwks_cached_at
    if jwks_cache is None or time.time() - jwks_cached_at > JWKS_TTL:
        async with httpx.AsyncClient() as client:
            resp = await client.get(JWKS_URL)
            resp.raise_for_status()
            jwks_cache = resp.json()
            jwks_cached_at = time.time()
    return jwks_cache


def get_public_key(jwks, kid: str):
    for key in jwks["keys"]:
        if key.get("kid") == kid:
            return RSAKey(key, ALGORITHM)
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Public key not found for kid"
    )
async def verify_token(token: str = Depends(oauth2_scheme)):
    try:
        jwks = await get_jwks()

        headers = jwt.get_unverified_header(token)
        kid = headers.get("kid")
        if not kid:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token missing kid"
            )

        public_key = get_public_key(jwks, kid)

        payload = jwt.decode(
            token,
            public_key,
            algorithms=[ALGORITHM],
            options={"verify_aud": False}
        )

        # ✅ normalize role / scope
        scopes = []
        if "scope" in payload:
            scopes = payload["scope"].split()
        elif "scp" in payload:
            scopes = payload["scp"]
        elif "roles" in payload:
            scopes = payload["roles"]

        payload["__roles__"] = scopes
        return payload

    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token"
        )
