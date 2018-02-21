package com.epam.page.object.generator.util;

import com.epam.page.object.generator.model.Selector;
import com.epam.page.object.generator.model.searchrule.ComplexInnerSearchRule;
import com.epam.page.object.generator.model.searchrule.ComplexSearchRule;
import com.epam.page.object.generator.model.searchrule.SearchRule;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.xsoup.Xsoup;

import java.util.List;

/**
 * This class responsible for extracting of inner {@link Elements} from {@link Element}.
 */
public class SearchRuleExtractor {

    private final static Logger logger = LoggerFactory.getLogger(SearchRuleExtractor.class);

    public SearchRuleExtractor() {
    }

    /**
     * This method returns all inner {@link Elements} according to {@link SearchRule}.
     *
     * @param element    {@link Element} that we are extracting from {@link Elements}.
     * @param searchRule {@link SearchRule} is a rule that get suitable {@link Selector}.
     * @return {@link Elements} from {@link Element} according to {@link SearchRule}
     */
    public Elements extractElementsFromElement(Element element, SearchRule searchRule) {
        Elements elements = extractElementsFromDocument(element, searchRule);
        if (searchRule instanceof ComplexSearchRule) {
            this.checkInnerElements(elements, searchRule);
        }
        return elements;
    }

    /**
     * This method extracts {@link Elements} from a Document node based on the first element of {@link SearchRule} list.
     */
    private Elements extractElementsFromDocument(Element element, SearchRule searchRule) {
        Selector selector = searchRule.getSelector();
        if (selector.isXpath()) {
            return Xsoup.compile(selector.getValue()).evaluate(element).getElements();
        } else if (selector.isCss()) {
            return element.select(selector.getValue());
        }
        throw new IllegalArgumentException("Selector type is unknown " + selector.toString());
    }

    /**
     * This method checks that elements based on {@link SearchRule} can be found in root element.
     * If not, an appropriate {@link ComplexInnerSearchRule} is skipped in order not to be added to annotation of an element.
     */
    private void checkInnerElements(Elements elements, SearchRule searchRule) {
        List<ComplexInnerSearchRule> rules = ((ComplexSearchRule) searchRule).getInnerSearchRules();
        elements.forEach(element -> {
            Element document = element.parent();
            rules.removeIf(logging(
                    item -> extractElementsFromDocument(document, item).isEmpty(),
                    item -> logger.warn("Incorrect search rule: " + item.toString())));
        });
    }

    private static <T> Predicate<T> logging(Predicate<T> p, Consumer<T> log) {
        return t -> {
            boolean result = p.test(t);
            if (result) {
                log.accept(t);
            }
            return result;
        };
    }
}
