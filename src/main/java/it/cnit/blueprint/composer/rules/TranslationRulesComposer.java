package it.cnit.blueprint.composer.rules;

import it.cnit.blueprint.composer.exceptions.TransRuleCompositionException;
import it.cnit.blueprint.composer.exceptions.TransRuleInvalidException;
import it.nextworks.nfvmano.catalogue.blueprint.elements.VsdNsdTranslationRule;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.NsDf;
import it.nextworks.nfvmano.libs.ifa.descriptors.nsd.Nsd;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class TranslationRulesComposer {

  public List<VsdNsdTranslationRule> compose(Nsd expNsd,
      List<VsdNsdTranslationRule> translationRules)
      throws TransRuleInvalidException, TransRuleCompositionException {
    log.info("Compose and check translation rules for expNsd: '{}'", expNsd.getNsdIdentifier());
    List<VsdNsdTranslationRule> newTranslationRules = new ArrayList<>();
    for (VsdNsdTranslationRule tr : translationRules) {
      try {
        tr.isValid();
      } catch (MalformattedElementException e) {
        throw new TransRuleInvalidException("Invalid translation rule " + tr.toString());
      }
      Optional<NsDf> optNsDf = expNsd.getNsDf().stream()
          .filter(nsDf -> nsDf.getNsDfId().equals(tr.getNsFlavourId())).findFirst();
      if (optNsDf.isPresent()) {
        boolean isNsLevelFound = optNsDf.get().getNsInstantiationLevel().stream()
            .anyMatch(nsLevel -> nsLevel.getNsLevelId().equals(tr.getNsInstantiationLevelId()));
        if (!isNsLevelFound) {
          String m = MessageFormatter.format("Invalid translation rule. NsLevel='{}' not found",
              tr.getNsInstantiationLevelId()).getMessage();
          log.error(m);
          throw new TransRuleInvalidException(m);
        }
      } else {
        String m = MessageFormatter.format("Invalid translation rule. NsDf='{}' not found",
            tr.getNsFlavourId()).getMessage();
        log.error(m);
        throw new TransRuleInvalidException(m);
      }
      VsdNsdTranslationRule newTr = new VsdNsdTranslationRule(
          tr.getInput(),
          null,
          expNsd.getNsdIdentifier(), expNsd.getVersion(), tr.getNsFlavourId(),
          tr.getNsInstantiationLevelId());
      try {
        newTr.isValid();
      } catch (MalformattedElementException e) {
        throw new TransRuleCompositionException(
            "Invalid translation rule after composition " + newTr.toString());
      }
      newTranslationRules.add(newTr);
    }
    return newTranslationRules;
  }

}
