# Deploy — VPS (Lightsail, sem Docker)

Este backend corre diretamente no VPS via `systemd` (Ubuntu 24.04) — sem Docker. Postgres corre no mesmo servidor. Os frontends (`zebratravel`, `zebraDM`) vão para a Vercel, não para este VPS.

## Servidor

- **Host**: ver `login.sh` na raiz do monorepo (não commitado — tem o IP e o caminho da chave `.pem`).
- **Utilizador**: `ubuntu` (sudo sem password).
- **RAM**: só 911MB — há uma swap de 2GB (`/swapfile`) como rede de segurança. O heap do Java está limitado (`-Xmx400m`) de propósito, ver `zebratravel-api.service`.
- **App**: `/home/ubuntu/webserver/zebratravel-api/`
  - `zebratravel-api.jar` — o JAR em execução (substituído a cada deploy)
  - `uploads/` — Media Library (`MEDIA_UPLOAD_DIR`)
  - `.env` — variáveis de ambiente (`chmod 600`, nunca commitado) — ver secção "Variáveis de ambiente"

## Serviços instalados

| Serviço | Pacote | Notas |
|---|---|---|
| Java | `openjdk-17-jre-headless` | |
| Base de dados | `postgresql` + `postgresql-contrib` (v16) | BD `zebratravel_db`, utilizador `zebratravel` |
| Proxy | `nginx` | config em `/etc/nginx/sites-available/api.zebratravel.net` |
| Conversão WebP | `webp` | ver `dev-notes.md` na raiz do monorepo, secção "5a. Media Library" |
| Firewall | `ufw` | só permite SSH, 80, 443 |

## `zebratravel-api.service` (systemd)

```ini
[Unit]
Description=ZebraTravel API (Spring Boot)
After=network.target postgresql.service

[Service]
User=ubuntu
EnvironmentFile=/home/ubuntu/webserver/zebratravel-api/.env
ExecStart=/usr/bin/java -Xmx400m -Xss512k -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -jar /home/ubuntu/webserver/zebratravel-api/zebratravel-api.jar
WorkingDirectory=/home/ubuntu/webserver/zebratravel-api
SuccessExitStatus=143
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

## Variáveis de ambiente (`.env` no servidor)

Não commitado (tem segredos). Contém: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `MEDIA_UPLOAD_DIR`, `CORS_ALLOWED_ORIGINS`, `FRONTEND_URL`, `VINTI4_CALLBACK_URL` — ver `application.properties` para a lista completa de variáveis que a app aceita.

**Por preencher assim que existirem** (ficaram com placeholders na primeira instalação):
- `CORS_ALLOWED_ORIGINS` — trocar pelos domínios reais da Vercel (`zebratravel` + `zebraDM`) assim que estiverem publicados.
- `FRONTEND_URL` — confirmar que é o domínio público real do `zebratravel`.
- Credenciais reais do Vinti4 (`VINTI4_POS_ID`, `VINTI4_POS_AUT_CODE`) — hoje usa os valores de sandbox do `application.properties`, trocar antes de aceitar pagamentos reais.

## Nginx

`/etc/nginx/sites-available/api.zebratravel.net` → proxy para `127.0.0.1:8080`. Só HTTP por agora — falta o certificado de origem do Cloudflare (`/etc/ssl/cloudflare/origin.crt`/`.key`) para ativar o bloco HTTPS (mesmo padrão usado no projeto rentacar).

## Deploy automático (GitHub Actions)

`.github/workflows/deploy.yml` — em cada push a `main`: build Maven → copia o JAR por `scp` → `sudo systemctl restart zebratravel-api` → espera o health check responder.

**Secrets necessários no repositório GitHub** (Settings → Secrets and variables → Actions):
- `HOST` — IP do servidor
- `USERNAME` — `ubuntu`
- `SSH_PRIVATE_KEY` — chave privada dedicada ao deploy (não é a chave de admin do Lightsail — foi gerada de propósito e a pública já está em `~/.ssh/authorized_keys` no servidor)
- `TARGET_PATH` — `/home/ubuntu/webserver/zebratravel-api/zebratravel-api.jar`

## Deploy manual (sem esperar pelo GitHub Actions)

```bash
./mvnw -q -B package -DskipTests
scp -i <chave> target/zebratravelB-0.0.1-SNAPSHOT.jar ubuntu@<HOST>:/home/ubuntu/webserver/zebratravel-api/zebratravel-api.jar
ssh -i <chave> ubuntu@<HOST> "sudo systemctl restart zebratravel-api"
```

## Operações comuns

```bash
# reiniciar
sudo systemctl restart zebratravel-api

# logs ao vivo
sudo journalctl -u zebratravel-api -f

# estado
sudo systemctl status zebratravel-api
```
