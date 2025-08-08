package com.sources.app.dao;

import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sources.app.entities.Hospital;

public class HospitalDAOTest {
    private HospitalDAO hospitalDAO = new HospitalDAO();
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testHospitalFromJsonFile() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/hospital.json");
        assertNotNull(is);
        Hospital hospitalFromJson = mapper.readValue(is, Hospital.class);
        assertNotNull(hospitalFromJson);
        assertEquals("General Hospital", hospitalFromJson.getName());
    }

    @Test
    public void testCreateHospitalFromJson() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/hospital.json");
        assertNotNull(is);
        Hospital hospitalFromJson = mapper.readValue(is, Hospital.class);
        Hospital createdHospital = hospitalDAO.create(
                hospitalFromJson.getName(),
                hospitalFromJson.getPhone(),
                hospitalFromJson.getEmail(),
                hospitalFromJson.getAddress(),
                hospitalFromJson.getEnabled()
        );
        assertNotNull(createdHospital);
        assertNotNull(createdHospital.getIdHospital());
        assertEquals(hospitalFromJson.getName(), createdHospital.getName());
    }
}