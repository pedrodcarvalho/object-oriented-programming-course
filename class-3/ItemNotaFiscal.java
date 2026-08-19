public class ItemNotaFiscal {
    // Atributos privados (encapsulamento)
    private String numero;
    private String codigo;
    private String nome;
    private double quantidade;
    private String unidade;
    private double precoUnitario;
    private double valorTotal;

    // Construtor padrão (sem parâmetros)
    public ItemNotaFiscal() {
    }

    // Construtor completo
    public ItemNotaFiscal(String numero, String codigo, String nome, double quantidade,
            String unidade, double precoUnitario, double valorTotal) {
        this.numero = numero;
        this.codigo = codigo;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.precoUnitario = precoUnitario;
        this.valorTotal = valorTotal;
    }

    // Métodos Getters e Setters
    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }
}
