# Multi-tenant OAuth2 Authentication/Authorization Server (Prototype)

This project provides a FastAPI prototype for your requested auth server features:

- OAuth2 password login + JWT
- Multi-tenancy (tenant ID and tenant name embedded in JWT)
- System user bootstrap (`sysadmin`)
- User registration service with CAPTCHA validation hook
- Two-step verification using SMS service hook
- Conditional national code in second step when user has multiple phones
- JWT claims include `roles`, `username`, `tenant_id`, `tenant_name`
- User APIs for own roles and permissions
- APIs to list users by role and by permission
- Impersonation endpoint for system admins
- Activity logs
- Active session listing and revoke-other-sessions
- User activity reporting endpoint (`last_login`) designed to be backed by Elasticsearch in production

## Run

```bash
pip install -e .[dev]
uvicorn app.main:app --reload
```

## Default System User

- username: `sysadmin`
- password: `sysadmin123`
- tenant: `system`

## Important Production Notes

This is a starter implementation. Before production use, you should:

1. Replace in-memory stores with persistent database tables.
2. Replace placeholder CAPTCHA validator and SMS sender with your real services.
3. Store activity logs and analytics in Elasticsearch.
4. Add robust permission checks per endpoint.
5. Rotate and secure JWT secrets, and add refresh tokens.
