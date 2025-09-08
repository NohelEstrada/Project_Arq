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

    @Test
    public void testGetByIdUpdateAndGetAll() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/sources/app/dao/hospital.json");
        assertNotNull(is);
        Hospital hospitalFromJson = mapper.readValue(is, Hospital.class);
        // create
        Hospital created = hospitalDAO.create(
                hospitalFromJson.getName() + System.currentTimeMillis(),
                hospitalFromJson.getPhone(),
                hospitalFromJson.getEmail(),
                hospitalFromJson.getAddress(),
                hospitalFromJson.getEnabled()
        );
        assertNotNull(created);
        Long id = created.getIdHospital();
        assertNotNull(id);
        // getById
        Hospital fetched = hospitalDAO.getById(id);
        assertNotNull(fetched);
        assertEquals(created.getName(), fetched.getName());
        // update
        fetched.setName(fetched.getName() + " Updated");
        Hospital updated = hospitalDAO.update(fetched);
        assertNotNull(updated);
        assertEquals(fetched.getName(), updated.getName());
        // getAll
        assertTrue(hospitalDAO.getAll() != null && !hospitalDAO.getAll().isEmpty());
    }
}