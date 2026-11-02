package br.com.oficina.shared.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DocumentTest {

    @Test
    void shouldCreateDocumentWithValidCpf() {
        Document document = new Document("529.982.247-25");

        assertThat(document.value()).isEqualTo("52998224725");
    }

    @Test
    void shouldCreateDocumentWithValidCnpj() {
        Document document = new Document("11.222.333/0001-81");

        assertThat(document.value()).isEqualTo("11222333000181");
    }

    @Test
    void shouldAcceptCpfWithoutFormatting() {
        Document document = new Document("52998224725");

        assertThat(document.value()).isEqualTo("52998224725");
    }

    @Test
    void shouldRejectInvalidCpf() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Document("111.111.111-11"));
    }

    @Test
    void shouldRejectInvalidCnpj() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Document("11.222.333/0001-00"));
    }

    @Test
    void shouldRejectDocumentWithInvalidLength() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Document("123456"));
    }

    @Test
    void shouldRejectNullDocument() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Document(null));
    }

    @Test
    void shouldRejectEmptyDocument() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Document(""));
    }

    @Test
    void shouldRejectBlankDocument() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Document("   "));
    }
}
