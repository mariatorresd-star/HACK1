package com.example.hack1base.sale.domain;

import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.domain.SaleService;
import com.example.hack1base.Sale.estructure.SaleRepository;
import com.example.hack1base.User.domain.Role;   // <<< IMPORTA TU ENUM
import com.example.hack1base.User.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private SaleService saleService;

    private User creator;
    private Sale saleBase;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setUsername("miraflores.user");
        creator.setEmail("miraflores.user@mail.com");
        creator.setRole(Role.BRANCH);             // <<< ENUM
        creator.setBranch("Miraflores");

        saleBase = new Sale();
        saleBase.setSku("OREO_CLASSIC_12");
        saleBase.setUnits(25);
        saleBase.setPrice(1.99);
        saleBase.setBranch("Miraflores");
        saleBase.setSoldAt(LocalDateTime.of(2025, 9, 12, 16, 30));
        saleBase.setCreatedBy(creator);
    }

    @Test
    @DisplayName("createSale debe guardar y devolver la venta")
    void createSale_ok() {
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        Sale result = saleService.createSale(saleBase);

        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("OREO_CLASSIC_12");
        assertThat(result.getUnits()).isEqualTo(25);
        assertThat(result.getPrice()).isEqualTo(1.99d);
        assertThat(result.getBranch()).isEqualTo("Miraflores");
        assertThat(result.getSoldAt()).isEqualTo(LocalDateTime.of(2025, 9, 12, 16, 30));
        assertThat(result.getCreatedBy().getUsername()).isEqualTo("miraflores.user");

        verify(saleRepository, times(1)).save(any(Sale.class));
        verifyNoMoreInteractions(saleRepository);
    }

    @Test
    @DisplayName("getAllSales debe retornar la lista de ventas")
    void getAllSales_ok() {
        User creator2 = new User();
        creator2.setUsername("user2");
        creator2.setEmail("user2@mail.com");
        creator2.setRole(Role.BRANCH);            // <<< ENUM
        creator2.setBranch("Miraflores");

        Sale s2 = new Sale();
        s2.setSku("OREO_DOUBLE");
        s2.setUnits(40);
        s2.setPrice(2.49);
        s2.setBranch("Miraflores");
        s2.setSoldAt(LocalDateTime.of(2025, 9, 13, 10, 0));
        s2.setCreatedBy(creator2);

        when(saleRepository.findAll()).thenReturn(List.of(saleBase, s2));

        List<Sale> all = saleService.getAllSales();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Sale::getSku).containsExactly("OREO_CLASSIC_12", "OREO_DOUBLE");
        verify(saleRepository, times(1)).findAll();
        verifyNoMoreInteractions(saleRepository);
    }

    @Test
    @DisplayName("getSaleById debe devolver Optional con la venta si existe")
    void getSaleById_found() {
        when(saleRepository.findById(100L)).thenReturn(Optional.of(saleBase));

        Optional<Sale> found = saleService.getSaleById(100L);

        assertThat(found).isPresent();
        assertThat(found.get().getSku()).isEqualTo("OREO_CLASSIC_12");
        verify(saleRepository, times(1)).findById(100L);
        verifyNoMoreInteractions(saleRepository);
    }

    @Test
    @DisplayName("getSaleById debe devolver Optional.empty si no existe")
    void getSaleById_empty() {
        when(saleRepository.findById(200L)).thenReturn(Optional.empty());

        Optional<Sale> found = saleService.getSaleById(200L);

        assertThat(found).isEmpty();
        verify(saleRepository, times(1)).findById(200L);
        verifyNoMoreInteractions(saleRepository);
    }

    @Test
    @DisplayName("updateSale debe copiar campos de saleDetails y guardar")
    void updateSale_ok() {
        Sale existing = new Sale();
        existing.setSku("OLD");
        existing.setUnits(1);
        existing.setPrice(0.99);
        existing.setBranch("OLD");
        existing.setSoldAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        existing.setCreatedBy(creator);

        when(saleRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        User other = new User();
        other.setUsername("other.user");
        other.setEmail("other@mail.com");
        other.setRole(Role.CENTRAL);
        other.setBranch("San Isidro");

        Sale details = new Sale();
        details.setSku("OREO_THINS");
        details.setUnits(32);
        details.setPrice(2.19);
        details.setBranch("San Isidro");
        details.setSoldAt(LocalDateTime.of(2025, 9, 3, 11, 5));
        details.setCreatedBy(other);

        Sale updated = saleService.updateSale(10L, details);

        ArgumentCaptor<Sale> captor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository, times(1)).findById(10L);
        verify(saleRepository, times(1)).save(captor.capture());
        verifyNoMoreInteractions(saleRepository);

        Sale toSave = captor.getValue();
        assertThat(toSave.getSku()).isEqualTo("OREO_THINS");
        assertThat(toSave.getUnits()).isEqualTo(32);
        assertThat(toSave.getPrice()).isEqualTo(2.19d);
        assertThat(toSave.getBranch()).isEqualTo("San Isidro");
        assertThat(toSave.getSoldAt()).isEqualTo(LocalDateTime.of(2025, 9, 3, 11, 5));
        assertThat(toSave.getCreatedBy().getUsername()).isEqualTo("other.user");

        assertThat(updated.getSku()).isEqualTo("OREO_THINS");
    }

    @Test
    @DisplayName("updateSale debe lanzar RuntimeException si no existe la venta")
    void updateSale_notFound() {
        when(saleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.updateSale(999L, saleBase))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Sale not found");

        verify(saleRepository, times(1)).findById(999L);
        verifyNoMoreInteractions(saleRepository);
    }

    @Test
    @DisplayName("deleteSale debe invocar deleteById en el repositorio")
    void deleteSale_ok() {
        doNothing().when(saleRepository).deleteById(77L);

        saleService.deleteSale(77L);

        verify(saleRepository, times(1)).deleteById(77L);
        verifyNoMoreInteractions(saleRepository);
    }
}

