import cx_Oracle

username = 'ADMIN'
password = 'admin123'
dsn = '64.225.58.196:1521/XEPDB1' 

try:
    connection = cx_Oracle.connect(username, password, dsn)
    print("Connecion to db successful")

    cursor = connection.cursor()
    
    cursor.execute('ALTER SESSION SET CURRENT_SCHEMA = "USUARIO"')
    cursor.execute("SELECT * FROM MEDICINE")
    for row in cursor:
        print(row)
    
except cx_Oracle.DatabaseError as e:
    print("Error in db connection:", e)
finally:
    if 'cursor' in locals() and cursor:
        cursor.close()
    if 'connection' in locals() and connection:
        connection.close()