package systems.glam.ix.proxy;

import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.programs.Discriminator;
import software.sava.core.tx.Instruction;

import java.util.List;

public interface IxProxy<A> {

  static <A> IxProxy<A> createProxy(final Discriminator srcDiscriminator,
                                    final Discriminator dstDiscriminator,
                                    final List<DynamicAccount<A>> dynamicAccounts,
                                    final List<IndexedAccountMeta> staticAccounts,
                                    final int[] indexes) {
    int numRemoved = 0;
    for (final int index : indexes) {
      if (index < 0) {
        ++numRemoved;
      }
    }
    return new IxProxyRecord<>(
        srcDiscriminator,
        dstDiscriminator,
        dynamicAccounts,
        staticAccounts,
        indexes,
        dynamicAccounts.size() + staticAccounts.size() + (indexes.length - numRemoved)
    );
  }

  Instruction mapInstruction(final AccountMeta invokedProgram,
                             final AccountMeta feePayer,
                             final A runtimeAccounts,
                             final Instruction instruction);

  default Instruction mapInstruction(final AccountMeta feePayer,
                                     final A runtimeAccounts,
                                     final Instruction instruction) {
    return mapInstruction(instruction.programId(), feePayer, runtimeAccounts, instruction);
  }

  Discriminator srcDiscriminator();

  Discriminator dstDiscriminator();
}
