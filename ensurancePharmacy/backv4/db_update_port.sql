-- Script para añadir la columna PORT a la tabla HOSPITALS
-- Esta columna almacenará el puerto del servidor API del hospital

-- Verificar si la columna ya existe antes de añadirla
DECLARE
  v_column_exists NUMBER := 0;
BEGIN
  SELECT COUNT(*) INTO v_column_exists
  FROM USER_TAB_COLUMNS
  WHERE TABLE_NAME = 'HOSPITALS' AND COLUMN_NAME = 'PORT';
  
  IF v_column_exists = 0 THEN
    EXECUTE IMMEDIATE 'ALTER TABLE HOSPITALS ADD PORT VARCHAR2(10)';
    DBMS_OUTPUT.PUT_LINE('Columna PORT añadida a la tabla HOSPITALS');
  ELSE
    DBMS_OUTPUT.PUT_LINE('La columna PORT ya existe en la tabla HOSPITALS');
  END IF;
END;
/

-- Comentario para la nueva columna
COMMENT ON COLUMN HOSPITALS.PORT IS 'Puerto del servidor API del hospital para conexiones desde el sistema de seguros';

-- Actualizar hospitales existentes con un puerto por defecto si es necesario
-- UPDATE HOSPITALS SET PORT = '8000' WHERE PORT IS NULL;

COMMIT;

-- Verificación
SELECT ID_HOSPITAL, NAME, PORT FROM HOSPITALS; 