import requests


class AuthClient:
    def __init__(self, auth_server_url, client_id, client_secret, scope="openid profile email"):
        self.auth_server_url = auth_server_url.rstrip("/")
        self.client_id = client_id
        self.client_secret = client_secret
        self.scope = scope
        self.token = None

    def get_token(self):
        """
        Lấy access_token từ Authorization Server bằng client_credentials
        (dùng khi server-to-server, không có user interaction)
        """
        token_url = f"{self.auth_server_url}/oauth2/token"
        data = {
            "grant_type": "client_credentials",
            "scope": self.scope
        }

        response = requests.post(
            token_url,
            data=data,
            auth=(self.client_id, self.client_secret)
        )

        if response.status_code != 200:
            raise Exception(f"Error fetching token: {response.status_code} {response.text}")

        self.token = response.json().get("access_token")
        return self.token

    def exchange_code(self, code: str, redirect_uri: str):
        """
        Đổi authorization_code lấy access_token (dùng khi user login qua /login)
        """
        token_url = f"{self.auth_server_url}/oauth2/token"
        data = {
            "grant_type": "authorization_code",
            "code": code,
            "redirect_uri": redirect_uri,
            "client_id": self.client_id,
            "client_secret": self.client_secret,
        }

        response = requests.post(token_url, data=data)
        if response.status_code != 200:
            raise Exception(f"Error exchanging code: {response.status_code} {response.text}")

        tokens = response.json()
        self.token = tokens.get("access_token")
        return tokens

    def get_headers(self):
        """
        Header Authorization để gọi API
        """
        if not self.token:
            self.get_token()
        return {"Authorization": f"Bearer {self.token}"}
