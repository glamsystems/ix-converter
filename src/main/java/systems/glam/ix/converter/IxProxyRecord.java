package systems.glam.ix.converter;

import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.programs.Discriminator;
import software.sava.core.tx.Instruction;

import java.util.Arrays;
import java.util.List;

record IxProxyRecord(Discriminator srcDiscriminator,
                     Discriminator dstDiscriminator,
                     List<IndexedAccountMeta> programAccounts,
                     List<IndexedAccountMeta> newAccounts,
                     int[] indexes,
                     int numAccounts) implements IxProxy {


  @Override
  public Instruction mapInstruction(final AccountMeta invokedProgram, final Instruction instruction) {
    final int discriminatorLength = srcDiscriminator.length();
    final int glamDiscriminatorLength = dstDiscriminator.length();
    final int lengthDelta = glamDiscriminatorLength - discriminatorLength;
    final int dataLength = instruction.len();
    final byte[] data;
    if (lengthDelta == 0) {
      data = new byte[dataLength];
      System.arraycopy(instruction.data(), instruction.offset(), data, 0, dataLength);
    } else {
      data = new byte[dataLength + lengthDelta];
      System.arraycopy(
          instruction.data(), instruction.offset() + discriminatorLength,
          data, discriminatorLength, dataLength - discriminatorLength
      );
    }
    dstDiscriminator.write(data, 0);

    final var mappedAccounts = new AccountMeta[numAccounts];
    for (final var programAccountMeta : programAccounts) {
      programAccountMeta.setAccount(mappedAccounts);
    }
    for (final var indexedAccountMeta : newAccounts) {
      indexedAccountMeta.setAccount(mappedAccounts);
    }

    final var accounts = instruction.accounts();
    final int numAccounts = accounts.size();

    int s = 0;
    int g;
    for (; s < indexes.length; ++s) {
      g = indexes[s];
      if (g >= 0) {
        mappedAccounts[g] = accounts.get(s);
      }
    }

    // Copy extra accounts.
    g = numAccounts;
    for (; s < numAccounts; ++s, ++g) {
      mappedAccounts[g] = accounts.get(s);
    }

    return Instruction.createInstruction(
        invokedProgram,
        Arrays.asList(mappedAccounts),
        data
    );
  }
}
