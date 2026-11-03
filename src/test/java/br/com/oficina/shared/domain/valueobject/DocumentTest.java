package br.com.oficina.shared.domain.valueobject;

import br.com.oficina.shared.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class DocumentTest {

    @Test
    void shouldCreateDocumentWithMaskedCpf() {
        Document document = new Document("529.982.247-25");

        assertThat(document.value()).isEqualTo("52998224725");
        assertThat(document.type()).isEqualTo(Document.Type.CPF);
        assertThat(document.formatted()).isEqualTo("529.982.247-25");
    }

    @Test
    void shouldCreateDocumentWithMaskedCnpj() {
        Document document = new Document("11.222.333/0001-81");

        assertThat(document.value()).isEqualTo("11222333000181");
        assertThat(document.type()).isEqualTo(Document.Type.CNPJ);
        assertThat(document.formatted()).isEqualTo("11.222.333/0001-81");
    }

    @Test
    void shouldAcceptDocumentWithoutMask() {
        assertThat(new Document("52998224725").value()).isEqualTo("52998224725");
    }

    @Test
    void documentsWithSameValueShouldBeEqual() {
        assertThat(new Document("529.982.247-25")).isEqualTo(new Document("52998224725"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"111.111.111-11", "11.222.333/0001-00", "123456", "abc"})
    void shouldRejectInvalidDocument(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Document(input))
                .withMessage("CPF ou CNPJ inválido.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldRejectBlankDocument(String input) {
        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> new Document(input))
                .withMessage("Documento não pode ser vazio.");
    }
}
