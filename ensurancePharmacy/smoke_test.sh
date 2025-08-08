#!/bin/bash

# Smoke test for Oracle Database
echo "Starting Oracle Database smoke test..."

# Wait for database to be ready
echo "Waiting for database to be ready..."
sleep 30

# Test database connectivity
echo "Testing database connectivity..."
sqlplus -s system/Pharmacy1234@localhost:1521/FREEPDB1 << EOF
SELECT 1 FROM DUAL;
EXIT;
EOF

if [ $? -eq 0 ]; then
    echo "✅ Database connectivity test passed"
else
    echo "❌ Database connectivity test failed"
    exit 1
fi

# Test basic query
echo "Testing basic query..."
sqlplus -s system/Pharmacy1234@localhost:1521/FREEPDB1 << EOF
CREATE TABLE smoke_test (id NUMBER);
INSERT INTO smoke_test VALUES (1);
SELECT * FROM smoke_test;
DROP TABLE smoke_test;
EXIT;
EOF

if [ $? -eq 0 ]; then
    echo "✅ Basic query test passed"
else
    echo "❌ Basic query test failed"
    exit 1
fi

echo "All smoke tests passed successfully! 🎉" 