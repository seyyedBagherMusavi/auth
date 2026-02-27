<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Registration</title>
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;600;700&family=Source+Code+Pro:wght@400;600&display=swap');
        :root {
            --bg: #0b0f1a;
            --card: #12182b;
            --text: #e6e9f2;
            --muted: #a7b0c4;
            --accent: #f6b73c;
            --accent-2: #5fd1b6;
            --stroke: #232a3f;
        }
        * { box-sizing: border-box; }
        body {
            margin: 0;
            font-family: "Space Grotesk", system-ui, sans-serif;
            color: var(--text);
            background:
                radial-gradient(1200px 600px at 15% -10%, #24305c 0%, rgba(36,48,92,0) 60%),
                radial-gradient(900px 500px at 90% 10%, #1b3b4d 0%, rgba(27,59,77,0) 55%),
                var(--bg);
        }
        a { color: inherit; text-decoration: none; }
        .wrap { max-width: 1100px; margin: 0 auto; padding: 32px 20px 64px; }
        .nav { display: flex; align-items: center; justify-content: space-between; }
        .badge {
            background: linear-gradient(135deg, var(--accent), #f9d976);
            color: #171717;
            padding: 6px 10px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
        }
        .hero { margin-top: 26px; display: grid; gap: 18px; }
        .hero h1 { margin: 0; font-size: 36px; }
        .hero p { margin: 0; color: var(--muted); }
        .grid {
            margin-top: 26px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 18px;
        }
        .card {
            background: var(--card);
            border: 1px solid var(--stroke);
            border-radius: 16px;
            padding: 18px;
        }
        .step {
            display: grid;
            grid-template-columns: 36px 1fr;
            gap: 12px;
            align-items: start;
            margin-bottom: 14px;
        }
        .step-num {
            width: 32px;
            height: 32px;
            border-radius: 10px;
            background: var(--accent-2);
            color: #0b1c18;
            display: grid;
            place-items: center;
            font-weight: 700;
        }
        textarea {
            width: 100%;
            min-height: 260px;
            padding: 12px;
            border-radius: 12px;
            border: 1px solid var(--stroke);
            background: #0f1424;
            color: var(--text);
            font-family: "Source Code Pro", ui-monospace, SFMono-Regular, Menlo, monospace;
            font-size: 12px;
        }
        input {
            width: 100%;
            padding: 10px 12px;
            border-radius: 10px;
            border: 1px solid var(--stroke);
            background: #0f1424;
            color: var(--text);
            font-family: inherit;
        }
        button {
            margin-top: 12px;
            padding: 10px 14px;
            border-radius: 10px;
            border: none;
            font-weight: 600;
            cursor: pointer;
            background: linear-gradient(135deg, var(--accent), #f9d976);
            color: #1d1d1d;
        }
        pre {
            margin: 12px 0 0;
            padding: 12px;
            background: #0f1424;
            border-radius: 12px;
            border: 1px solid var(--stroke);
            color: #cfe2ff;
            font-family: "Source Code Pro", ui-monospace, SFMono-Regular, Menlo, monospace;
            font-size: 12px;
            white-space: pre-wrap;
        }
        .links { display: flex; gap: 14px; color: var(--muted); font-weight: 500; }
        .hint { font-size: 13px; color: var(--muted); margin-top: 8px; }
    </style>
</head>
<body>
<div class="wrap">
    <div class="nav">
        <div class="badge">Registration</div>
        <div class="links">
            <a href="/">Home</a>
            <a href="/login">Login</a>
        </div>
    </div>

    <div class="hero">
        <h1>Register Tenant + User</h1>
        <p>Use the JSON payload below to create a tenant, user, and phone entries in one call.</p>
    </div>

    <div class="grid">
        <div class="card">
            <div class="step">
                <div class="step-num">1</div>
                <div>
                    <strong>Prepare Payload</strong>
                    <p>Fill tenant data, user, and phone list.</p>
                </div>
            </div>
            <form id="registerForm">
                <label for="registerTenantId">Tenant ID (optional)</label>
                <input id="registerTenantId" name="registerTenantId" placeholder="1" />
                <textarea id="registerPayload">
{
  "tenantCode": "acme",
  "tenantName": "Acme Corp",
  "tenantBaseUrl": "https://acme.example",
  "username": "demo.user",
  "password": "Password123",
  "systemUser": false,
  "phones": [
    { "phoneNumber": "+1-555-0101", "nationalCode": "US", "preferred": true }
  ],
  "roleCodes": ["USER"]
}
                </textarea>
                <button type="submit">Register</button>
            </form>
            <div class="hint">Endpoint: POST /api/auth/register</div>
            <pre id="registerResult">Waiting for registration...</pre>
        </div>

        <div class="card">
            <div class="step">
                <div class="step-num">2</div>
                <div>
                    <strong>Next Steps</strong>
                    <p>After registering, continue with two-step login and token usage.</p>
                </div>
            </div>
            <p class="hint">Suggested flow:</p>
            <pre>
1) POST /api/auth/login
2) POST /api/auth/login/select-phone (if multiple phones)
3) POST /api/auth/verify-2fa
4) Use access token with protected APIs
            </pre>
            <div class="hint">You can jump directly to the two-step login UI.</div>
            <a href="/login"><button type="button">Go to Two-Step Login</button></a>
        </div>

        <div class="card">
            <div class="step">
                <div class="step-num">i</div>
                <div>
                    <strong>Service Config</strong>
                    <p>Captcha + SMS endpoints from config.</p>
                </div>
            </div>
            <label>Captcha URL</label>
            <input value="${captchaUrl}" readonly />
            <label>Captcha Validate URL</label>
            <input value="${captchaValidateUrl}" readonly />
            <label>SMS Sender API</label>
            <input value="${smsSenderApiUrl}" readonly />
        </div>
    </div>
</div>

<script>
    const registerForm = document.getElementById("registerForm");
    const registerPayload = document.getElementById("registerPayload");
    const registerResult = document.getElementById("registerResult");
    const registerTenantId = document.getElementById("registerTenantId");

    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        registerResult.textContent = "Sending...";
        let body;
        try {
            body = JSON.parse(registerPayload.value);
        } catch (err) {
            registerResult.textContent = "Invalid JSON: " + err.message;
            return;
        }
        try {
            const headers = { "Content-Type": "application/json" };
            if (registerTenantId.value) {
                headers["X-Actor-Tenant-Id"] = registerTenantId.value;
            }
            const res = await fetch("/api/auth/register", {
                method: "POST",
                headers,
                body: JSON.stringify(body)
            });
            const text = await res.text();
            registerResult.textContent = res.status + " " + res.statusText + "\n" + (text || "(empty)");
        } catch (err) {
            registerResult.textContent = "Error: " + err.message;
        }
    });
</script>
</body>
</html>
