# Configuración de Puertos para las APIs

Este sistema permite configurar los puertos que se utilizan para conectarse a los backends de Ensurance y Pharmacy.

## Cómo funciona

El sistema ahora incluye una interfaz para seleccionar los puertos que se utilizarán para conectarse a:

1. **Backend de Ensurance** (predeterminado: 8080)
2. **Backend de Pharmacy** (predeterminado: 8081)

## Opciones de configuración

### Al iniciar la aplicación

1. Cuando inicias la aplicación, se mostrará un diálogo para configurar los puertos.
2. Puedes ingresar los números de puerto que desees utilizar.
3. Si marcas la opción "Recordar mi elección", no se te volverá a mostrar este diálogo.

### Desde el menú de administración

Si necesitas cambiar los puertos después de la configuración inicial:

1. Inicia sesión como administrador
2. Haz clic en "Admin" en la barra de navegación
3. Selecciona "Configurar Puertos API" en el menú desplegable
4. Ingresa los nuevos valores de puertos y guarda la configuración

## Recomendaciones

- **Backend de Ensurance**: Usa el puerto que configuraste al iniciar el servidor Java (normalmente 8080)
- **Backend de Pharmacy**: Usa el puerto que configuraste para el otro servidor Java (normalmente 8081)

## Solución de problemas

Si experimentas errores de conexión:

1. Verifica que los puertos configurados coincidan con los puertos en los que están ejecutándose tus servidores backend
2. Asegúrate de que los servidores backend estén iniciados antes de intentar conectarte
3. Si estás detrás de un firewall, verifica que los puertos estén abiertos

## Persistencia de la configuración

La configuración de puertos se guarda en el almacenamiento local del navegador (localStorage) para que se mantenga incluso si cierras el navegador.

Para reiniciar la configuración, puedes borrar el almacenamiento local del navegador o usar el menú de administración para configurar nuevos valores. 