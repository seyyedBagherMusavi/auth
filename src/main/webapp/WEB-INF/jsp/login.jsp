<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Two-Step Login</title>
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
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
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
        label { font-size: 13px; color: var(--muted); display: block; margin-bottom: 6px; }
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
    </style>
</head>
<body>
<div class="wrap">
    <div class="nav">
        <div class="badge">Two-Step Login</div>
        <div class="links">
            <a href="/">Home</a>
            <a href="/register">Register</a>
        </div>
    </div>

    <div class="hero">
        <h1>Three-Step Login Flow</h1>
        <p>Step 1 validates username, password, and captcha. If multiple phones exist, choose national code to send OTP. Then verify OTP.</p>
    </div>

    <div class="grid">
        <div class="card">
            <div class="step">
                <div class="step-num">1</div>
                <div>
                    <strong>Validate Credentials</strong>
                    <p>POST /api/auth/login</p>
                </div>
            </div>
            <form id="stepOneForm">
                <label for="tenantId">Tenant ID</label>
                <input id="tenantId" name="tenantId" placeholder="1" required />
                <label for="username">Username</label>
                <input id="username" name="username" placeholder="demo.user" required />
                <label for="password">Password</label>
                <input id="password" name="password" type="password" placeholder="Password123" required />
                <label for="captchaToken">Captcha Token</label>
                <input id="captchaToken" name="captchaToken" placeholder="demo-captcha" required />
                <button type="submit">Send Step 1</button>
            </form>
            <pre id="stepOneResult">Waiting for step 1...</pre>
        </div>

        <div class="card">
            <div class="step">
                <div class="step-num">2</div>
                <div>
                    <strong>Select National Code</strong>
                    <p>POST /api/auth/login/select-phone</p>
                </div>
            </div>
            <form id="selectPhoneForm">
                <label for="nationalCode">National Code</label>
                <input id="nationalCode" name="nationalCode" placeholder="US" list="nationalCodeOptions" />
                <datalist id="nationalCodeOptions"></datalist>
                <button type="submit">Send OTP</button>
            </form>
            <pre id="selectPhoneResult">Waiting for step 2...</pre>
        </div>

        <div class="card">
            <div class="step">
                <div class="step-num">3</div>
                <div>
                    <strong>Verify OTP</strong>
                    <p>POST /api/auth/verify-2fa</p>
                </div>
            </div>
            <form id="stepTwoForm">
                <label for="code">OTP Code</label>
                <input id="code" name="code" placeholder="123456" required />
                <button type="submit">Verify</button>
            </form>
            <pre id="stepTwoResult">Waiting for step 3...</pre>
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
    const stepOneForm = document.getElementById("stepOneForm");
    const selectPhoneForm = document.getElementById("selectPhoneForm");
    const stepTwoForm = document.getElementById("stepTwoForm");
    const stepOneResult = document.getElementById("stepOneResult");
    const selectPhoneResult = document.getElementById("selectPhoneResult");
    const stepTwoResult = document.getElementById("stepTwoResult");
    const nationalCodeInput = document.getElementById("nationalCode");
    const nationalCodeOptions = document.getElementById("nationalCodeOptions");
    let currentUsername = "";
    let currentTenantId = "";

    stepOneForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        stepOneResult.textContent = "Sending...";
        stepTwoResult.textContent = "Waiting for step 3...";
        selectPhoneResult.textContent = "Waiting for step 2...";
        currentUsername = document.getElementById("username").value;
        currentTenantId = document.getElementById("tenantId").value;
        const payload = {
            username: currentUsername,
            password: document.getElementById("password").value,
            captchaToken: document.getElementById("captchaToken").value
        };
        try {
            const res = await fetch("/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json", "X-Actor-Tenant-Id": currentTenantId },
                body: JSON.stringify(payload)
            });
            const text = await res.text();
            stepOneResult.textContent = res.status + " " + res.statusText + "\n" + (text || "(empty)");
            if (!res.ok) return;
            let data;
            try { data = JSON.parse(text); } catch { return; }
            if (data.status === "NEED_NATIONAL_CODE") {
                selectPhoneResult.textContent = "Select national code and send OTP.";
                const candidates = data.candidates || [];
                nationalCodeOptions.innerHTML = "";
                candidates.forEach((c) => {
                    if (!c.nationalCode) return;
                    const option = document.createElement("option");
                    option.value = c.nationalCode;
                    option.label = c.phone ? (c.nationalCode + " " + c.phone) : c.nationalCode;
                    nationalCodeOptions.appendChild(option);
                });
                if (candidates[0] && candidates[0].nationalCode) {
                    nationalCodeInput.value = candidates[0].nationalCode;
                }
                return;
            }
            if (data.status === "OTP_SENT") {
                selectPhoneResult.textContent = "OTP sent. Step 2 not required. Proceed to Step 3.";
            }
        } catch (err) {
            stepOneResult.textContent = "Error: " + err.message;
        }
    });

    selectPhoneForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        selectPhoneResult.textContent = "Sending...";
        const payload = {
            username: currentUsername,
            nationalCode: nationalCodeInput.value
        };
        try {
            const res = await fetch("/api/auth/login/select-phone", {
                method: "POST",
                headers: { "Content-Type": "application/json", "X-Actor-Tenant-Id": currentTenantId },
                body: JSON.stringify(payload)
            });
            const text = await res.text();
            selectPhoneResult.textContent = res.status + " " + res.statusText + "\n" + (text || "(empty)");
        } catch (err) {
            selectPhoneResult.textContent = "Error: " + err.message;
        }
    });

    stepTwoForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        stepTwoResult.textContent = "Sending...";
        const payload = {
            username: currentUsername,
            code: document.getElementById("code").value
        };
        try {
            const res = await fetch("/api/auth/verify-2fa", {
                method: "POST",
                headers: { "Content-Type": "application/json", "X-Actor-Tenant-Id": currentTenantId },
                body: JSON.stringify(payload)
            });
            const text = await res.text();
            stepTwoResult.textContent = res.status + " " + res.statusText + "\n" + (text || "(empty)");
        } catch (err) {
            stepTwoResult.textContent = "Error: " + err.message;
        }
    });
</script>
</body>
</html>
