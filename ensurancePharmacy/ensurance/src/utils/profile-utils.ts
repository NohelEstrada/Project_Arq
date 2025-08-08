/**
 * Verifica si faltan campos obligatorios en el perfil de usuario según su rol
 * @param profile Perfil del usuario
 * @returns true si faltan campos obligatorios, false si el perfil está completo
 */
export const checkMissingRequiredFields = (profile: any): boolean => {
  // Solución temporal: siempre retornar false para evitar redirecciones a completar perfil
  return false;
  
  /*
  // Código original (actualmente deshabilitado)
  if (!profile || !profile.role) return true;
  
  // Si el usuario ya ha completado su perfil (marcado en localStorage), considerarlo completo
  if (profile.profile_completed === true) return false;
  
  // Campos básicos requeridos para todos los roles
  const basicFields = ["name", "email", "password", "cui", "phone", "address", "birthDate"];
  const missingBasicFields = basicFields.some(field => !profile[field]);
  if (missingBasicFields) return true;
  
  // Campos específicos según el rol
  const roleFields: Record<string, string[]> = {
    'admin': ['licenseNumber'],
    'employee': ['hospitalName'],
    'patient': ['insuranceNumber', 'allergies'],
    'interconnection': ['systemAccess']
  };
  
  // Si no hay campos específicos para este rol, el perfil está completo
  if (!roleFields[profile.role]) return false;
  
  // Verificar campos específicos del rol
  const profileData = profile.profile_data || {};
  return roleFields[profile.role].some(field => !profileData[field]);
  */
}; 