echo "Testing POST /login..."
LOGIN_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{"username": "user1", "password": "password123"}' \
  "${BASE_URL}/login")

if [ -n "$LOGIN_RESPONSE" ]; then
  echo "Login successful: $LOGIN_RESPONSE"
else
  echo "Login failed."
  exit 1
fi