# Email Configuration Troubleshooting Guide

## Issue: Confirmation emails not being sent in production

### Step 1: Verify Render Environment Variables

1. **Go to your Render Dashboard**:
   - Navigate to: https://dashboard.render.com
   - Select your `tuoficio-backend` service

2. **Check Environment Variables**:
   Go to "Environment" tab and verify these variables are set:
   
   ```
   EMAIL_USERNAME=tuoficiopracticasupervisada@gmail.com
   EMAIL_PASSWORD=twzf lugx xoyd aswk
   BACKEND_URL=https://tuoficio-backend.onrender.com
   FRONTEND_URL=https://tuoficio-frontend.onrender.com
   ```

3. **If missing, add them**:
   - Click "Add Environment Variable"
   - Add each one manually
   - Click "Save Changes"
   - **Important**: After adding, you must redeploy the service

### Step 2: Verify Gmail App Password

The password `twzf lugx xoyd aswk` appears to be a Gmail App Password (correct format).

To verify or regenerate:
1. Go to: https://myaccount.google.com/apppasswords
2. Login with: tuoficiopracticasupervisada@gmail.com
3. Check if the app password exists or create a new one
4. If you create a new one, update it in Render's environment variables

### Step 3: Check Render Logs

After redeploying, test registration and check logs:

1. Go to Render Dashboard → tuoficio-backend → Logs
2. Look for these log messages during registration:
   ```
   📧 Enviando email de confirmación a: [email]
   ✅ Email enviado exitosamente
   ```
   
3. If you see errors, they'll start with:
   ```
   ⚠️ Error al enviar email (continuando con el registro):
   ```

### Step 4: Common Error Messages

**Authentication failed (535 error)**:
- Wrong EMAIL_USERNAME or EMAIL_PASSWORD
- Gmail blocking sign-in attempts
- 2FA enabled without App Password

**Connection timeout**:
- Render firewall blocking SMTP ports
- SMTP host/port incorrect

**BadCredentialsException**:
- App password incorrectly formatted (should have no spaces)

### Step 5: Test Email Sending

After fixing environment variables, test by:
1. Register a new user at: https://tuoficio-frontend.onrender.com/auth/registro
2. Check the email inbox
3. Check Render logs for success/error messages

### Alternative: Use SendGrid or Another Email Service

If Gmail continues to have issues, consider using SendGrid (free tier: 100 emails/day):

1. Sign up at: https://sendgrid.com/
2. Get API key
3. Update EmailServiceImpl to use SendGrid API instead of SMTP
4. More reliable for production environments

---

## Quick Command to Check Logs

```bash
# If you have Render CLI installed
render logs -s tuoficio-backend -n 100
```

## Need More Help?

Check the full logs in Render dashboard and look for:
- "Error enviando email HTML"
- Any MessagingException messages
- Authentication failures
