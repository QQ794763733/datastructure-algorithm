package cn.machine.geek.structure.set;

import java.util.Comparator;

/**
 * @Author: MachineGeek
 * @Description: Tree集合（红黑树版本）
 * @Email: 794763733@qq.com
 * @Date: 2021/2/4
 */
public class TreeSet<E> {
    private Node<E> root;
    private int size;
    private Comparator<E> comparator;

    /**
     * @Author: MachineGeek
     * @Description: 节点
     * @Date: 2021/1/5
     * @Return:
     */
    public class Node<E> {
        private E element;
        private Node<E> parent;
        private Node<E> left;
        private Node<E> right;
        private boolean red = true;

        public Node(E element, Node<E> parent) {
            this.element = element;
            this.parent = parent;
        }
    }

    /**
     * @Author: MachineGeek
     * @Description: 遍历操作抽象类
     * @Date: 2020/12/28
     * @Return:
     */
    public static abstract class Visitor<E> {
        boolean stop;

        protected abstract boolean operate(E element);
    }

    /**
     * @param
     * @Author: MachineGeek
     * @Description: 长度
     * @Date: 2021/2/4
     * @Return: int
     */
    public int size() {
        return size;
    }

    /**
     * @param
     * @Author: MachineGeek
     * @Description: 是否为空
     * @Date: 2021/2/4
     * @Return: boolean
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * @param
     * @Author: MachineGeek
     * @Description: 清空元素
     * @Date: 2021/2/4
     * @Return: void
     */
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * @param element
     * @Author: MachineGeek
     * @Description: 包含元素
     * @Date: 2021/2/4
     * @Return: boolean
     */
    public boolean contains(E element) {
        return getNode(element) != null;
    }

    /**
     * @param element
     * @Author: MachineGeek
     * @Description: 增加节点
     * @Date: 2021/1/5
     * @Return: void
     */
    public void add(E element) {
        // 根节点直接添加
        if (size == 0) {
            root = new Node<>(element, null);
            afterAdd(root);
        } else {
            // 向左右查找并记录parent节点
            Node<E> temp = root;
            Node<E> parent = null;
            int value = 0;
            while (temp != null) {
                parent = temp;
                value = compare(temp.element, element);
                if (value == 0) {
                    temp.element = element;
                    return;
                }
                if (value > 0) {
                    temp = temp.left;
                } else {
                    temp = temp.right;
                }
            }
            // 使用节点元素与新增元素的比较值value值来判断放左边还是右边
            Node<E> node = new Node<>(element, parent);
            if (value > 0) {
                parent.left = node;
            } else {
                parent.right = node;
            }
            // 新增后的处理
            afterAdd(node);
        }
        size++;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 红黑树添加节点后的处理
     * @Date: 2021/1/25
     * @Return: void
     */
    private void afterAdd(Node<E> node) {
        // 如果是根节点直接染黑返回
        Node<E> parent = node.parent;
        if (parent == null) {
            node.red = false;
            return;
        }
        // 如果父节点是黑色不用处理，如果是红色则需要处理。
        if (parent.red) {
            Node<E> grandParent = parent.parent;
            Node<E> uncle = getBrother(parent);
            // 如果叔父节点是黑色，需要进行旋转操作，并设置颜色。
            if (isBlack(uncle)) {
                // L
                if (parent == parent.parent.left) {
                    // LL
                    if (node == parent.left) {
                        parent.red = false;
                        // LR
                    } else {
                        leftRotate(parent);
                        node.red = false;
                    }
                    grandParent.red = true;
                    rightRotate(grandParent);
                    // R
                } else {
                    // RL
                    if (node == parent.left) {
                        rightRotate(parent);
                        node.red = false;
                        // RR
                    } else {
                        parent.red = false;
                    }
                    grandParent.red = true;
                    leftRotate(grandParent);
                }
                // 如果叔父节点是红色，需要向上递归染色。
            } else {
                parent.red = false;
                uncle.red = false;
                grandParent.red = true;
                afterAdd(grandParent);
            }
        }
    }

    /**
     * @param element
     * @Author: MachineGeek
     * @Description: 删除节点
     * @Date: 2021/1/5
     * @Return: void
     */
    public void remove(E element) {
        Node<E> node = getNode(element);
        // 节点为空直接返回
        if (node == null) {
            return;
        }
        // 如果节点左右子节点都不为空，寻找一个前驱节点赋值到自己，并让前驱结点删除。
        if (node.left != null && node.right != null) {
            Node<E> predecessor = predecessorNode(node);
            node.element = predecessor.element;
            node = predecessor;
        }
        // 寻找这个将要被删除的节点的子节点作为替代节点
        Node<E> replace = node.left != null ? node.left : node.right;
        // 有子节点的情况处理
        if (replace != null) {
            replace.parent = node.parent;
            if (replace.parent == null) {
                root = replace;
            } else if (node.parent.left == node) {
                node.parent.left = replace;
            } else {
                node.parent.right = replace;
            }
            afterRemove(replace);
            // 根节点的情况处理
        } else if (node.parent == null) {
            root = null;
            afterRemove(node);
            // 无子节点的情况处理
        } else {
            if (node.parent.left == node) {
                node.parent.left = null;
            } else {
                node.parent.right = null;
            }
            afterRemove(node);
        }
        // 删除后的情况处理
        size--;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 红黑树删除节点后的处理
     * @Date: 2021/2/1
     * @Return: void
     */
    private void afterRemove(Node<E> node) {
        // 如果删除的节点（或者替代节点）是红色，染黑后直接返回。
        if (isRed(node)) {
            node.red = false;
            return;
        }
        // 如果是根节点直接返回
        Node<E> parent = node.parent;
        if (parent == null) {
            return;
        }
        // 被删除的节点是黑色叶子节点，获取它的兄弟节点
        boolean left = parent.left == null || node == parent.left;
        Node<E> brother = left ? parent.right : parent.left;
        // 被删除的节点在左边，兄弟节点在右边
        if (left) {
            // 如果右边的兄弟节点为红色，则对父节点左旋，重新赋值兄弟节点。
            if (brother.red) {
                brother.red = false;
                parent.red = true;
                leftRotate(parent);
                brother = parent.right;
            }
            // 如果兄弟节点的左右节点都是黑色，则没有可借节点，父节点染黑，兄弟节点染红。
            if (isBlack(brother.left) && isBlack(brother.right)) {
                boolean red = parent.red;
                brother.red = true;
                parent.red = false;
                // 如果父节点是黑色，则会下溢，需要重新处理
                if (!red) {
                    afterRemove(parent);
                }
                // 兄弟节点至少有一个红色子节点
            } else {
                // 如果兄弟的右子节点是黑色，兄弟先进行一次右旋
                if (isBlack(brother.right)) {
                    rightRotate(brother);
                    brother = parent.right;
                }
                // 染色
                brother.red = parent.red;
                if (brother.right != null) {
                    brother.right.red = false;
                }
                parent.red = false;
                // 对父节点进行左旋转
                leftRotate(parent);
            }
            // 被删除的节点在右边，兄弟节点在左边
        } else {
            // 如果左边的兄弟节点为红色，则对父节点右旋，重新赋值兄弟节点。
            if (brother.red) {
                brother.red = false;
                parent.red = true;
                rightRotate(parent);
                brother = parent.left;
            }
            // 如果兄弟节点的左右节点都是黑色，则没有可借节点，父节点染黑，兄弟节点染红。
            if (isBlack(brother.left) && isBlack(brother.right)) {
                boolean red = parent.red;
                brother.red = true;
                parent.red = false;
                // 如果父节点是黑色，则会下溢，需要重新处理
                if (!red) {
                    afterRemove(parent);
                }
                // 兄弟节点至少有一个红色子节点
            } else {
                // 如果兄弟的左子节点是黑色，兄弟先进行一次左旋
                if (isBlack(brother.left)) {
                    leftRotate(brother);
                    brother = parent.left;
                }
                // 染色
                brother.red = parent.red;
                if (brother.left != null) {
                    brother.left.red = false;
                }
                parent.red = false;
                // 对父节点进行右旋转
                rightRotate(parent);
            }
        }
    }

    /**
     * @param element
     * @Author: MachineGeek
     * @Description: 查找节点
     * @Date: 2021/1/5
     * @Return: cn.machine.geek.structure.tree.BinarySearchTree<E>.Node<E>
     */
    public Node<E> getNode(E element) {
        Node<E> temp = root;
        while (temp != null) {
            int value = compare(temp.element, element);
            if (value == 0) {
                return temp;
            }
            if (value > 0) {
                temp = temp.left;
            } else {
                temp = temp.right;
            }
        }
        return temp;
    }


    /**
     * @param element1
     * @param element2
     * @Author: MachineGeek
     * @Description: 比较元素
     * @Date: 2021/1/5
     * @Return: int
     */
    private int compare(E element1, E element2) {
        if (comparator != null) {
            return comparator.compare(element1, element2);
        }
        return ((Comparable) element1).compareTo(element2);
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 获取兄弟节点
     * @Date: 2021/1/25
     * @Return: cn.machine.geek.structure.tree.RedBlackTree<E>.Node<E>
     */
    private Node<E> getBrother(Node<E> node) {
        if (node == null || node.parent == null) {
            return null;
        } else if (node == node.parent.left) {
            return node.parent.right;
        } else {
            return node.parent.left;
        }
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 对节点进行左旋
     * @Date: 2020/12/31
     * @Return: cn.machine.geek.structure.tree.AVLTree<E>.Node<E>
     */
    private void leftRotate(Node<E> node) {
        Node<E> right = node.right;
        node.right = right.left;
        right.left = node;
        right.parent = node.parent;
        node.parent = right;
        if (node.right != null) {
            node.right.parent = node;
        }
        if (right.parent == null) {
            root = right;
        } else if (right.parent.left == node) {
            right.parent.left = right;
        } else {
            right.parent.right = right;
        }
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 对节点进行右旋
     * @Date: 2020/12/31
     * @Return: cn.machine.geek.structure.tree.AVLTree<E>.Node<E>
     */
    private void rightRotate(Node<E> node) {
        Node<E> left = node.left;
        node.left = left.right;
        left.right = node;
        left.parent = node.parent;
        node.parent = left;
        if (node.left != null) {
            node.left.parent = node;
        }
        if (left.parent == null) {
            root = left;
        } else if (left.parent.left == node) {
            left.parent.left = left;
        } else {
            left.parent.right = left;
        }
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 中序
     * @Date: 2021/1/5
     * @Return: void
     */
    private void inorder(Node<E> node, Visitor<E> visitor) {
        if (null == node || visitor.stop) {
            return;
        }
        inorder(node.left, visitor);
        if (visitor.stop) {
            return;
        }
        visitor.stop = visitor.operate(node.element);
        inorder(node.right, visitor);
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 判断节点是否为黑色
     * @Date: 2021/2/2
     * @Return: boolean
     */
    private boolean isBlack(Node<E> node) {
        return node == null || !node.red;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 找到当前节点的中序遍历前驱节点
     * @Date: 2021/1/5
     * @Return: cn.machine.geek.structure.tree.BinarySearchTree<E>.Node<E>
     */
    private Node<E> predecessorNode(Node<E> node) {
        if (node == null) {
            return null;
        }
        if (node.left != null) {
            node = node.left;
            while (node.right != null) {
                node = node.right;
            }
            return node;
        }
        while (node.parent != null && node.parent.left == node) {
            node = node.parent;
        }
        return node.parent;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 找到当前节点的中序遍历后继节点
     * @Date: 2021/1/5
     * @Return: cn.machine.geek.structure.tree.BinarySearchTree<E>.Node<E>
     */
    private Node<E> subsequentNode(Node<E> node) {
        if (node == null) {
            return null;
        }
        if (node.right != null) {
            node = node.right;
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }
        while (node.parent != null && node.parent.right == node) {
            node = node.parent;
        }
        return node.parent;
    }

    /**
     * @param node
     * @Author: MachineGeek
     * @Description: 判断节点是否为红色
     * @Date: 2021/2/2
     * @Return: boolean
     */
    private boolean isRed(Node<E> node) {
        return node != null && node.red;
    }

    /**
     * @param
     * @Author: MachineGeek
     * @Description: 遍历接口
     * @Date: 2021/2/4
     * @Return: void
     */
    public void traversal(Visitor<E> visitor) {
        inorder(root, visitor);
    }
}
