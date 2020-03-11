package org.openmbee.sdvc.cameo.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.openmbee.sdvc.cameo.CameoConstants;
import org.openmbee.sdvc.cameo.CameoNodeType;
import org.openmbee.sdvc.core.config.ContextHolder;
import org.openmbee.sdvc.core.objects.ElementsRequest;
import org.openmbee.sdvc.core.objects.ElementsResponse;
import org.openmbee.sdvc.core.services.NodeChangeInfo;
import org.openmbee.sdvc.core.services.NodeGetInfo;
import org.openmbee.sdvc.data.domains.scoped.Node;
import org.openmbee.sdvc.json.ElementJson;
import org.springframework.stereotype.Service;

@Service("cameoViewService")
public class CameoViewService extends CameoNodeService {

    public ElementsResponse getDocuments(String projectId, String refId, Map<String, String> params) {
        ContextHolder.setContext(projectId, refId);
        List<Node> documents = this.nodeRepository.findAllByNodeType(CameoNodeType.DOCUMENT.getValue());
        ElementsResponse res = this.getViews(projectId, refId, buildRequest(nodeGetHelper.convertNodesToMap(documents).keySet()), params);
        for (ElementJson e: res.getElements()) {
            Optional<ElementJson> parent = nodeGetHelper.getFirstRelationshipOfType(e, CameoNodeType.GROUP.getValue(), CameoConstants.OWNERID);
            if (parent.isPresent()) {
                e.put("_groupId", parent.get().getId());
            }
        }
        return res;
    }

    public ElementsResponse getView(String projectId, String refId, String elementId, Map<String, String> params) {
        return this.getViews(projectId, refId, buildRequest(elementId), params);
    }

    public ElementsResponse getViews(String projectId, String refId, ElementsRequest req, Map<String, String> params) {
        ElementsResponse res = this.read(projectId, refId, req, params);
        for (ElementJson element: res.getElements()) {
            if (cameoHelper.isView(element)) {
                List<String> ownedAttributeIds = (List) element.get(CameoConstants.OWNEDATTRIBUTEIDS);
                ElementsResponse ownedAttributes = this.read(element.getProjectId(), element.getRefId(),
                    buildRequest(ownedAttributeIds), params);
                List<ElementJson> sorted = nodeGetHelper.sort(ownedAttributeIds, ownedAttributes.getElements());
                List<Map> childViews = new ArrayList<>();
                for (ElementJson attr : sorted) {
                    String childId = (String) attr.get(CameoConstants.TYPEID);
                    if ("Property".equals(attr.getType()) && childId != null && !childId.isEmpty()) {
                        Map<String, String> child = new HashMap<>();
                        child.put(ElementJson.ID, childId);
                        child.put(CameoConstants.AGGREGATION, (String) attr.get(CameoConstants.AGGREGATION));
                        child.put(CameoConstants.PROPERTYID, attr.getId());
                        childViews.add(child);
                    }
                }
                element.put(CameoConstants.CHILDVIEWS, childViews);
            }
        }
        return res;
    }

    public ElementsResponse getGroups(String projectId, String refId, Map<String, String> params) {
        ContextHolder.setContext(projectId, refId);
        List<Node> groups = this.nodeRepository.findAllByNodeType(CameoNodeType.GROUP.getValue());
        ElementsResponse res = this.read(projectId, refId, buildRequest(nodeGetHelper.convertNodesToMap(groups).keySet()), params);
        for (ElementJson e: res.getElements()) {
            Optional<ElementJson> parent = nodeGetHelper.getFirstRelationshipOfType(e, CameoNodeType.GROUP.getValue(), CameoConstants.OWNERID);
            if (parent.isPresent()) {
                e.put("_parentId", parent.get().getId());
            }
        }
        return res;
}

    @Override
    public void extraProcessPostedElement(ElementJson element, Node node, NodeChangeInfo info) {
        //handle _childViews
        List<Map<String, String>> newChildViews = (List)element.remove(CameoConstants.CHILDVIEWS);
        if (newChildViews == null) {
            super.extraProcessPostedElement(element, node, info);
            return;
        }
        //gather data on "old" attributes
        List<String> oldOwnedAttributeIds = (List)element.get(CameoConstants.OWNEDATTRIBUTEIDS);
        //use helper to get access to Nodes
        NodeGetInfo oldInfo = nodeGetHelper.processGetJson(buildRequest(oldOwnedAttributeIds).getElements(), null);
        List<PropertyData> oldProperties = new ArrayList<>();
        Map<String, PropertyData> oldPropertiesTypeMapping = new HashMap<>(); //typeId to PropertyData
        for (String oldOwnedAttributeId: oldOwnedAttributeIds) {
            if (!oldInfo.getActiveElementMap().containsKey(oldOwnedAttributeId)) {
                continue; //property doesn't exist anymore? indicates existing model inconsistency
            }
            Node oldNode = oldInfo.getExistingNodeMap().get(oldOwnedAttributeId);
            ElementJson oldJson = oldInfo.getActiveElementMap().get(oldOwnedAttributeId);
            PropertyData oldData = new PropertyData();
            oldData.setPropertyJson(oldJson);
            oldData.setPropertyNode(oldNode);
            oldProperties.add(oldData);
            String typeId = (String)oldJson.get(CameoConstants.TYPEID);
            if (typeId == null || typeId.isEmpty()) {
                continue; //property has no type
            }
            oldPropertiesTypeMapping.put(typeId, oldData);
        }
        ElementsResponse oldTypeJsons = this.read(element.getProjectId(), element.getRefId(), buildRequest(oldPropertiesTypeMapping.keySet()),
            Collections.EMPTY_MAP); //include project usages when finding types
        for (ElementJson oldType: oldTypeJsons.getElements()) {
            oldPropertiesTypeMapping.get(oldType.getId()).setTypeJson(oldType);
            oldPropertiesTypeMapping.get(oldType.getId()).setView(cameoHelper.isView(oldType));
        }
        //now oldProperties is a list of existing property data in existing order (can include ones with
        //      no type or types that're not views

        //go through requested _childView changes
        Optional<ElementJson> p = nodePostHelper.getFirstRelationshipOfType(element, CameoNodeType.PACKAGE.getValue(), CameoConstants.OWNERID);
        String packageId = p.isPresent() ? p.get().getId() : "holding_bin"; //get first package owner to put association in
        List<PropertyData> newProperties = new ArrayList<>();
        Map<String, PropertyData> newPropertiesTypeMapping = new HashMap<>();
        List<String> newAttributeIds = new ArrayList<>();
        for (Map<String, String> newChildView: newChildViews) {
            String typeId = newChildView.get(ElementJson.ID);
            if (oldPropertiesTypeMapping.containsKey(typeId)) {
                newProperties.add(oldPropertiesTypeMapping.get(typeId));
                newPropertiesTypeMapping.put(typeId, oldPropertiesTypeMapping.get(typeId));
                newAttributeIds.add(oldPropertiesTypeMapping.get(typeId).getPropertyNode().getNodeId());
                continue; //existing property and type, reuse
            }
            //create new properties and association
            PropertyData newProperty = createElementsForView(newChildView.get(CameoConstants.AGGREGATION), typeId, element.getId(), packageId, info);
            newProperties.add(newProperty);
            newPropertiesTypeMapping.put(typeId, newProperty);
            newAttributeIds.add(newProperty.getPropertyNode().getNodeId());
        }
        //go through old attributes and add back any that wasn't to a view and delete ones that's to a view but not in newProperties
        for (PropertyData oldProperty: oldProperties) {
            if (!oldProperty.isView()) {
                newProperties.add(oldProperty);
                if (oldProperty.getTypeJson() != null) {
                    newPropertiesTypeMapping.put(oldProperty.getTypeJson().getId(), oldProperty);
                }
                newAttributeIds.add(oldProperty.getPropertyNode().getNodeId());
                continue;
            }
            if (newPropertiesTypeMapping.containsKey(oldProperty.getTypeJson().getId())) {
                continue; //already added
            }
            //delete oldProperty and associated things
            Node oldPropertyNode = oldProperty.getPropertyNode();
            ElementJson oldPropertyJson = oldProperty.getPropertyJson();
            nodePostHelper.processElementDeleted(oldPropertyJson, oldPropertyNode, info);
            //TODO need to get association and association property and delete those too
        }
        element.put(CameoConstants.OWNEDATTRIBUTEIDS, newAttributeIds);
        super.extraProcessPostedElement(element, node, info);
    }

    private PropertyData createElementsForView(String aggregation, String typeId, String parentId, String packageId, NodeChangeInfo info) {
        //create new properties and association
        Node newPropertyNode = new Node();
        Node newAssocNode = new Node();
        Node newAssocPropertyNode = new Node();
        String newPropertyId = UUID.randomUUID().toString();
        String newAssocId = UUID.randomUUID().toString();
        String newAssocPropertyId = UUID.randomUUID().toString();
        ElementJson newPropertyJson = cameoHelper.createProperty(newPropertyId, "", parentId, aggregation, typeId,newAssocId);
        ElementJson newAssocJson = cameoHelper.createAssociation(newAssocId, packageId, newAssocPropertyId, newPropertyId);
        ElementJson newAssocPropertyJson = cameoHelper.createProperty(newAssocPropertyId, "", newAssocJson.getId(), "none", parentId, newAssocId);
        nodePostHelper.processElementAdded(newPropertyJson, newPropertyNode, info);
        nodePostHelper.processElementAdded(newAssocJson, newAssocNode, info);
        nodePostHelper.processElementAdded(newAssocPropertyJson, newAssocPropertyNode, info);
        super.extraProcessPostedElement(newPropertyJson, newPropertyNode, info);
        super.extraProcessPostedElement(newAssocJson, newAssocNode, info);
        super.extraProcessPostedElement(newAssocPropertyJson, newAssocPropertyNode, info);
        PropertyData newProperty = new PropertyData();
        newProperty.setAssocJson(newAssocJson);
        newProperty.setAssocNode(newAssocNode);
        newProperty.setPropertyJson(newPropertyJson);
        newProperty.setPropertyNode(newPropertyNode);
        newProperty.setAssocPropertyNode(newAssocPropertyNode);
        newProperty.setAssocPropertyJson(newAssocPropertyJson);
        newProperty.setView(true);
        return newProperty;
    }
}
