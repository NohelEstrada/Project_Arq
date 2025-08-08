// Script de prueba para verificar el endpoint de AppointmentMade
const axios = require('axios');

async function testAppointmentCreation() {
  // Configuración
  const baseUrl = 'http://192.168.0.4:8080';
  const endpoint = '/api/appointmentmade';
  
  // Datos de prueba
  const testData = {
    idCita: Date.now(), // Usamos timestamp actual como ID numérico
    idUser: 1, // Usar un ID de usuario que exista en el sistema
    appointmentMadeDate: new Date().toISOString().split('T')[0] // Fecha actual en formato YYYY-MM-DD
  };
  
  console.log('Intentando crear cita con datos:', JSON.stringify(testData, null, 2));
  
  try {
    // Intento con /api/ en la URL
    console.log('POST a:', baseUrl + endpoint);
    const response = await axios.post(baseUrl + endpoint, testData, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    console.log('Respuesta exitosa:', response.status);
    console.log('Datos de respuesta:', response.data);
  } catch (error) {
    console.error('Error en la petición:', error.message);
    
    if (error.response) {
      console.log('Estado de error:', error.response.status);
      console.log('Datos de error:', error.response.data);
    }
    
    // Intentar con URL alternativa (sin /api/)
    try {
      console.log('\nIntentando URL alternativa sin /api/');
      console.log('POST a:', baseUrl.replace('/api', '') + '/appointmentmade');
      
      const altResponse = await axios.post(baseUrl.replace('/api', '') + '/appointmentmade', testData, {
        headers: {
          'Content-Type': 'application/json'
        }
      });
      
      console.log('Respuesta exitosa (alt):', altResponse.status);
      console.log('Datos de respuesta (alt):', altResponse.data);
    } catch (altError) {
      console.error('Error también en URL alternativa:', altError.message);
      
      if (altError.response) {
        console.log('Estado de error (alt):', altError.response.status);
        console.log('Datos de error (alt):', altError.response.data);
      }
    }
  }
}

// Ejecutar la prueba
testAppointmentCreation().catch(err => {
  console.error('Error general:', err);
}); 