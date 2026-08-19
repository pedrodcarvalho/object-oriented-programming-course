import java.util.Scanner;

public class ItemNotaFiscalTest {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);

        // 1. Lê a quantidade total de itens (N)
        int n = Integer.parseInt(teclado.nextLine().trim());

        double totalAcumulado = 0.0;

        // 2. Loop para ler os N itens (7 linhas para cada item)
        for (int i = 0; i < n; i++) {
            String numero = teclado.nextLine().trim();
            String codigo = teclado.nextLine().trim();
            String nome = teclado.nextLine().trim();
            double quantidade = Double.parseDouble(teclado.nextLine().trim());
            String unidade = teclado.nextLine().trim();
            double precoUnitario = Double.parseDouble(teclado.nextLine().trim());
            double valorTotalItem = Double.parseDouble(teclado.nextLine().trim());

            // Cria a instância do item
            ItemNotaFiscal item = new ItemNotaFiscal(
                    numero, codigo, nome, quantidade, unidade, precoUnitario, valorTotalItem);

            // Acumula o valor total dos itens
            totalAcumulado += item.getValorTotal();
        }

        // 3. Lê o valor total final da nota (última linha)
        double totalLido = Double.parseDouble(teclado.nextLine().trim());

        // 4. Imprime os totais
        System.out.println("------------------------------------");
        System.out.printf("Valor total acumulado: R$ %.2f%n", totalAcumulado);
        System.out.printf("Valor total lido:      R$ %.2f%n", totalLido);
        System.out.println("------------------------------------");

        teclado.close();
    }
}
