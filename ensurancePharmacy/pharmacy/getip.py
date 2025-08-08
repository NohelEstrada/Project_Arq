import socket

def get_local_ip():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # Connect to an external IP address (this doesn't actually send data)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
    except Exception:
        ip = "127.0.0.1"
    finally:
        s.close()
    return ip

if __name__ == "__main__":
    ip = get_local_ip()
    print("Local IP Address:", ip)
    
    # Write the IP to a .env file
    with open('.env', 'w') as env_file:
        env_file.write(f"VUE_APP_API_HOST={ip}\n")
        env_file.write(f"VUE_APP_IP={ip}\n")
    print(".env file created successfully!")