echo "Starting test"
for i in $(seq 1 100)
do  
curl -s -o /dev/null http://localhost/devtools -d "{\"field\": $i }"
done
echo "test done"
