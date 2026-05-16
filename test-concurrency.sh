#!/bin/bash

URL="http://localhost:8080/api/bookings"
REQUESTS=500
USER_ID=1
TICKET_CATEGORY_ID=1

echo "Starting $REQUESTS concurrent booking requests..."

for i in $(seq 1 $REQUESTS)
do
  IDEMPOTENCY_KEY="test-key-$(uuidgen 2>/dev/null || echo $RANDOM-$RANDOM)"
  JSON_DATA='{
    "userId": '$USER_ID',
    "idempotencyKey": "'$IDEMPOTENCY_KEY'",
    "totalPrice": 100.00,
    "status": "RECEIVED",
    "bookingItems": [
      {
        "ticketCategory": { "id": '$TICKET_CATEGORY_ID' },
        "quantity": 1,
        "unitPrice": 100.00
      }
    ]
  }'

  curl -X POST "$URL" \
       -H "Content-Type: application/json" \
       -d "$JSON_DATA" \
       -s -o /dev/null -w "%{http_code}\n" &
done

wait
echo "Finished all requests."
