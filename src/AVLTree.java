
/**
 *
 * AVLTree
 *
 * An implementation of a AVL Tree with
 * distinct integer keys and info
 *
 */

public class AVLTree {

	private static final ExtLeaf extLeaf = new ExtLeaf();
	private IAVLNode root;
	private IAVLNode min_node;
	private IAVLNode max_node;
	// constructor
	public AVLTree() {
		this.root = AVLTree.extLeaf;
	}


	/**
	 * public boolean empty()
	 * <p>
	 * returns true if and only if the tree is empty
	 */
	//if the root is an external leaf it's an empty tree
	public boolean empty() {
		return root==extLeaf;
	}

	/**
	 * public String search(int k)
	 * <p>
	 * returns the info of an item with key k if it exists in the tree
	 * otherwise, returns null
	 */
	public String search(int k) {
		if (this.empty()) {                   // return null in case the tree is empty
			return null;
		}
		IAVLNode node = this.root;            // if the tree isn't empty we need to search k
		while (node != AVLTree.extLeaf) {
			if (node.getKey() == k) {         // if root.key == k
				return node.getValue();
			}
			else {                            // if root.key > k going left
				if (node.getKey() > k) {
					node = node.getLeft();
				}
				else {
					node = node.getRight();   // if root.key < k going right
				}
			}
		}
		return null;
	}

	/**
	 * public int insert(int k, String i)
	 * <p>
	 * inserts an item with key k and info i to the AVL tree.
	 * the tree must remain valid (keep its invariants).
	 * returns the number of rebalancing operations, or 0 if no rebalancing operations were necessary.
	 * promotion/rotation - counted as one rebalnce operation, double-rotation is counted as 2.
	 * returns -1 if an item with key k already exists in the tree.
	 */
	public int insert(int k, String i) {
		AVLNode y = (AVLNode) (this.TreePosition(root, k));

		AVLNode node = new AVLNode(k, i);         // create new AVLNode for the key = k

		if (y == null) {                          // if the tree is empty insert the node as the root and return 0
			node.setHeight(0);
			node.setLeft(AVLTree.extLeaf);
			node.setRight(AVLTree.extLeaf);
			node.setParent(null);
			node.fixSize();
			this.setRootForTree(node, this);
			return 0;
		}
		if (k == y.getKey()) {                    // if the node already exists return -1
			return -1;
		}

		node.setParent(y);                        // set node's parent to be y

		if (node.getKey() < y.getKey()) {         // insert node as left son
			y.setLeft(node);
		} else {                                  // insert node a right son
			y.setRight(node);
		}
		node.setRight(AVLTree.extLeaf);           // setting external leaves
		node.setLeft(AVLTree.extLeaf);
		node.setHeight(0);                        // setting node height to 0
		node.setSize(1);

		if (node.getKey() < this.min_node.getKey()) {  // check if needed to update the min reference
			this.min_node = node;
		}
		if (node.getKey() > this.max_node.getKey()) {  // check if needed to update the max reference
			this.max_node = node;
		}

		y.fixSize();      // correct node.parent size (+1)

		// Rebalancing Process
		int steps = 0;                               // if y isn't a leaf the tree is balanced so return 0
		if (y.getHeight() == 0) {                    // if y is a leaf we need to rebalance the tree
			steps = InsertBalance(y);
		}
		else {                 // we need to continue the path to the root in order to maintain size field
			while (y != null) {
				y.fixSize();
				y = (AVLNode)y.getParent();
			}
		}
		return steps;
	}
	/** function for rebalance after insertion  */
	public int InsertBalance(IAVLNode y){
		int steps = 0;
		int y_left_rank_diff = ((AVLNode) y).rank_difference(y.getLeft());       // calc rank difference between node and sons
		int y_right_rank_diff = ((AVLNode) y).rank_difference(y.getRight());


		// while node is not 1-1, 1-2 or 2-1
		while (!((y_left_rank_diff == 1 && y_right_rank_diff == 1) || (y_left_rank_diff == 1 && y_right_rank_diff == 2)
				|| (y_left_rank_diff == 2 && y_right_rank_diff == 1))) {
			// for 0-1 or 1-0 node we need to promote y
			if ((y_left_rank_diff == 1 && y_right_rank_diff == 0) || (y_left_rank_diff == 0 && y_right_rank_diff == 1)) {
				y.setHeight(y.getHeight()+1);
				steps++;
				((AVLNode) y).fixSize();
				// if we are in the root, exit loop
				if(y.getParent() == null) {
					this.root = y;
					break;
				}
			}

			// for 0-2 node
			else {
				if (y_left_rank_diff == 0 && y_right_rank_diff == 2) {
					// calc rank differences for y left son and his sons to decided which rotate to do
					// current node: y.getLeft()
					int y_leftleft_rank_diff = ((AVLNode) y.getLeft()).rank_difference(y.getLeft().getLeft());
					int y_leftright_rank_diff = ((AVLNode) y.getLeft()).rank_difference(y.getLeft().getRight());

					// for 1-2 node we need to rotate right
					if (y_leftleft_rank_diff == 1 && y_leftright_rank_diff == 2) {
						IAVLNode curr = rotate_right(y, y.getLeft());

						curr.getRight().setHeight(curr.getRight().getHeight()-1);
						fixParentPointer(curr, y.getKey());
						steps = steps+2;
						((AVLNode) curr.getRight()).fixSize();
						((AVLNode) curr).fixSize();
						break;

					}
					// for 2-1 node we need double rotation: left-right rotation
					if (y_leftleft_rank_diff == 2 && y_leftright_rank_diff == 1) {
						IAVLNode curr = rotate_left_right(y, y.getLeft(), y.getLeft().getRight());

						curr.setHeight(curr.getHeight()+1);
						curr.getLeft().setHeight(curr.getLeft().getHeight()-1);
						curr.getRight().setHeight(curr.getRight().getHeight()-1);
						fixParentPointer(curr, y.getKey());
						steps= steps+5;
						((AVLNode) curr.getLeft()).fixSize();
						((AVLNode) curr.getRight()).fixSize();
						((AVLNode) curr).fixSize();
						break;
					}
				}
				// for 2-0 nodes
				else {
					if (y_left_rank_diff == 2 && y_right_rank_diff == 0) {

						// calc rank differences for y right son and his sons to decided which rotate to do
						// current node: y.getLeft()
						int y_rightleft_rank_diff =  ((AVLNode) y.getRight()).rank_difference(y.getRight().getLeft());
						int y_rightright_rank_diff = ((AVLNode) y.getRight()).rank_difference(y.getRight().getRight());

						// for 1-2 node we need double rotation: right-left rotation
						if (y_rightleft_rank_diff == 1 && y_rightright_rank_diff == 2) {
							IAVLNode curr = rotate_right_left(y, y.getRight(), y.getRight().getLeft());
							curr.setHeight(curr.getHeight()+1);
							curr.getLeft().setHeight(curr.getLeft().getHeight()-1);
							curr.getRight().setHeight(curr.getRight().getHeight()-1);
							fixParentPointer(curr, y.getKey());
							steps= steps+5;
							((AVLNode) curr.getLeft()).fixSize();
							((AVLNode) curr.getRight()).fixSize();
							((AVLNode) curr).fixSize();
							break;

						}

						// for 2-1 node we need one rotation: left rotation
						if (y_rightleft_rank_diff == 2 && y_rightright_rank_diff == 1) {
							IAVLNode curr = rotate_left(y, y.getRight());
							curr.getLeft().setHeight(curr.getLeft().getHeight()-1);
							fixParentPointer(curr, y.getKey());
							((AVLNode) curr.getLeft()).fixSize();
							((AVLNode) curr).fixSize();
							steps= steps+2;
							break;
						}
					}
				}
			}
			y = y.getParent();
			y_left_rank_diff = ((AVLNode) y).rank_difference(y.getLeft());   // calc rank difference between node and left son
			y_right_rank_diff = ((AVLNode) y).rank_difference(y.getRight()); // calc rank difference between node and right son

		}

		while(y != null) {  // we need to continue the path to the root in order to maintain size field
			((AVLNode) y).fixSize();
			y = y.getParent();
		}
		return steps;
	}

	/** function that connect the current node's parent to the current node */
	public void fixParentPointer(IAVLNode curr, int curr_key){
		if(curr.getParent() != null) {           // if curr is not the root
			IAVLNode parent = curr.getParent();  // connect curr.parent with curr
			if (parent.getRight().getKey() == curr_key) {
				parent.setRight(curr);
			} else {
				parent.setLeft(curr);
			}
		}
		else {
			this.root = curr;                   // if curr is the root, change the root
		}

	}
	/** function for left rotation */
	public IAVLNode rotate_left(IAVLNode curr, IAVLNode right_son) {
		IAVLNode tmp = right_son.getLeft();
		right_son.setLeft(curr);
		right_son.setParent(curr.getParent());
		curr.setParent(right_son);
		curr.setRight(tmp);
		tmp.setParent(curr);
		return right_son;

	}
	/** function for right rotation */
	public IAVLNode rotate_right(IAVLNode curr, IAVLNode left_son) {
		IAVLNode tmp = left_son.getRight();
		left_son.setRight(curr);
		left_son.setParent(curr.getParent());
		curr.setParent(left_son);
		curr.setLeft(tmp);
		tmp.setParent(curr);
		return left_son;
	}
	/** function for right-left rotation */
	public IAVLNode rotate_right_left(IAVLNode curr, IAVLNode right_son, IAVLNode rightleft_son) {
		IAVLNode tmp_left = rightleft_son.getLeft();
		IAVLNode tmp_right = rightleft_son.getRight();

		rightleft_son.setParent(curr.getParent());
		curr.setParent(rightleft_son);
		rightleft_son.setLeft(curr);
		curr.setRight(tmp_left);
		tmp_left.setParent(curr);

		rightleft_son.setRight(right_son);
		right_son.setParent(rightleft_son);
		right_son.setLeft(tmp_right);
		tmp_right.setParent(right_son);
		return rightleft_son;

	}
	/** function for left-right rotation */
	public IAVLNode rotate_left_right(IAVLNode curr, IAVLNode left_son, IAVLNode leftright_son) {
		IAVLNode tmp_left = leftright_son.getLeft();
		IAVLNode tmp_right = leftright_son.getRight();

		leftright_son.setParent(curr.getParent());

		leftright_son.setLeft(left_son);
		left_son.setParent(leftright_son);
		leftright_son.setRight(curr);
		curr.setParent(leftright_son);

		left_son.setRight(tmp_left);
		tmp_left.setParent(left_son);
		curr.setLeft(tmp_right);
		tmp_right.setParent(curr);
		return leftright_son;

	}
	/** function for rebalance after deletion  */
	public int DeleteBalance(IAVLNode y){

		int steps=0;
		int y_left_rank_diff = ((AVLNode) y).rank_difference(y.getLeft());       // calc rank difference between node and sons
		int y_right_rank_diff = ((AVLNode) y).rank_difference(y.getRight());

		// while node is not 1-1, 1-2 or 2-1
		while (!((y_left_rank_diff == 1 && y_right_rank_diff == 1) || (y_left_rank_diff == 1 && y_right_rank_diff == 2)
				|| (y_left_rank_diff == 2 && y_right_rank_diff == 1))) {

			// for 2-2 node
			if (y_left_rank_diff == 2 && y_right_rank_diff == 2) {
				y.setHeight(y.getHeight() - 1);
				steps=steps+1;
				if(y.getParent() == null) {
					this.root = y;
					break;
				}
			}
			else {
				// for 3-1 node
				if (y_left_rank_diff == 3 && y_right_rank_diff == 1) {

					// calc rank differences for y left son and his sons to decided which rotate to do
					// current node: y.getLeft()
					int y_rightleft_rank_diff =  ((AVLNode) y.getRight()).rank_difference(y.getRight().getLeft());
					int y_rightright_rank_diff = ((AVLNode) y.getRight()).rank_difference(y.getRight().getRight());

					// for 1-1 node we need to rotate left
					if (y_rightleft_rank_diff == 1 && y_rightright_rank_diff == 1) {
						IAVLNode curr = rotate_left(y, y.getRight());
						curr.setHeight(curr.getHeight()+1);
						curr.getLeft().setHeight(curr.getLeft().getHeight()-1);
						fixParentPointer(curr, y.getKey());
						((AVLNode) curr.getLeft()).fixSize();
						((AVLNode) curr).fixSize();
						steps= steps+3;
						break;
					}
					// for 2-1 node we need to rotate left
					if (y_rightleft_rank_diff == 2 && y_rightright_rank_diff == 1) {
						IAVLNode curr = rotate_left(y, y.getRight());
						curr.getLeft().setHeight(curr.getLeft().getHeight()-2);
						fixParentPointer(curr, y.getKey());
						((AVLNode) curr.getLeft()).fixSize();
						((AVLNode) curr).fixSize();
						y=curr;
						steps+=2;
					}
					// for 1-2 node we need to rotate right-left
					else if (y_rightleft_rank_diff == 1 && y_rightright_rank_diff == 2) {
						IAVLNode curr = rotate_right_left(y, y.getRight(), y.getRight().getLeft());
						curr.setHeight(curr.getHeight()+1);
						curr.getLeft().setHeight(curr.getLeft().getHeight()-2);
						curr.getRight().setHeight(curr.getRight().getHeight()-1);
						fixParentPointer(curr, y.getKey());
						((AVLNode) curr.getLeft()).fixSize();
						((AVLNode) curr.getRight()).fixSize();
						((AVLNode) curr).fixSize();
						y=curr;
						steps+=5;
					}
				}
				else {
					// for 1-3 node
					if (y_left_rank_diff == 1 && y_right_rank_diff == 3) {

						// calc rank differences for y left son and his sons to decided which rotate to do
						// current node: y.getLeft()
						int y_leftleft_rank_diff = ((AVLNode) y.getLeft()).rank_difference(y.getLeft().getLeft());
						int y_leftright_rank_diff = ((AVLNode) y.getLeft()).rank_difference(y.getLeft().getRight());

						// for 1-1 node we need to rotate left
						if (y_leftleft_rank_diff == 1 && y_leftright_rank_diff == 1) {
							IAVLNode curr = rotate_right(y, y.getLeft());
							curr.setHeight(curr.getHeight()+1);
							curr.getRight().setHeight(curr.getRight().getHeight() - 1);
							fixParentPointer(curr, y.getKey());
							((AVLNode) curr.getRight()).fixSize();
							steps= steps + 3;
							break;
						}
						// for 2-1 node we need to rotate left
						if (y_leftleft_rank_diff == 2 && y_leftright_rank_diff == 1) {
							IAVLNode curr = rotate_left_right(y, y.getLeft(), y.getLeft().getRight());
							curr.setHeight(curr.getHeight() + 1);
							curr.getRight().setHeight(curr.getRight().getHeight() - 2);
							curr.getLeft().setHeight(curr.getLeft().getHeight() - 1);
							fixParentPointer(curr, y.getKey());
							((AVLNode) curr.getRight()).fixSize();
							((AVLNode) curr.getLeft()).fixSize();
							((AVLNode) curr).fixSize();
							y=curr;
							steps += 5;
						}
						// for 1-2 node we need to rotate right-left
						else if (y_leftleft_rank_diff == 1 && y_leftright_rank_diff == 2) {

							IAVLNode curr = rotate_right(y, y.getLeft());
							curr.getRight().setHeight(curr.getRight().getHeight() - 2);
							fixParentPointer(curr, y.getKey());
							((AVLNode) curr.getRight()).fixSize();
							((AVLNode) curr).fixSize();
							y=curr;
							steps += 2;
						}
					}
				}

			}
			// if we are in the root, exit loop
			if (y.getParent() == null) {
				this.root = y;
				break;
			}
			y = y.getParent();
			y_left_rank_diff = ((AVLNode) y).rank_difference(y.getLeft());   // calc rank difference between node and left son
			y_right_rank_diff = ((AVLNode) y).rank_difference(y.getRight()); // calc rank difference between node and right son
		}
		if(y == this.root) {  // we maintained size field all the way to the root and can finish
			((AVLNode) y).fixSize();
			return steps;
		}
		else{                 // we need to continue the path to the root in order to maintain size field
			while(y != null){
				((AVLNode) y).fixSize();
				y = y.getParent();
			}
		}
		return steps;
	}

	/**
	 * public int delete(int k)
	 * <p>
	 * deletes an item with key k from the binary tree, if it is there;
	 * the tree must remain valid (keep its invariants).
	 * returns the number of rebalancing operations, or 0 if no rebalancing operations were needed.
	 * demotion/rotation - counted as one rebalnce operation, double-rotation is counted as 2.
	 * returns -1 if an item with key k was not found in the tree.
	 */
	public int delete(int k) {
		if (search(k) == null) {
			return -1;
		}
		IAVLNode y = TreePosition(root, k);      // node.key = k

		if(y == this.root && y.getHeight()==0){  // there is only root in the tree and we delete it
			this.root = AVLTree.extLeaf;
			this.min_node = AVLTree.extLeaf;
			this.max_node = AVLTree.extLeaf;
			return 0;
		}
		int y_key = y.getKey();                  // saving y.key() for Min/Max tests later

		if (y.getLeft().getHeight() != -1 && y.getRight().getHeight() != -1) {  // if the node has 2 sons

			IAVLNode s = successor(y);                                          // find the successor and replace it with node
			int s_key = s.getKey();
			String s_info = s.getValue();
			((AVLNode) s).setKey(y.getKey());
			((AVLNode) s).setValue(y.getValue());
			((AVLNode) y).setKey(s_key);
			((AVLNode) y).setValue(s_info);
			y=s;

		}
		if (y.getParent()==null && y.getHeight()==1) {                          // if node is root and have 1 child
			if (y.getRight() == AVLTree.extLeaf) {
				this.root = y.getLeft();
				y.getLeft().setParent(null);
				y.setLeft(AVLTree.extLeaf);
				y.setRight(AVLTree.extLeaf);
				y=this.root;
			} else {
				IAVLNode s = successor(y);
				this.root = s;
				s.setParent(null);
				s.setLeft(AVLTree.extLeaf);
				s.setRight(AVLTree.extLeaf);
				y=this.root;
			}
			((AVLNode) this.root).fixSize();
			return 0;
		}
		else if (y.getHeight() == 0) {                  //  if the node is a leaf and not root

			if (isLeftSon(y)) {                         // check if node is left son
				y.getParent().setLeft(AVLTree.extLeaf);
			}                                           // if the node is a right son
			else {
				y.getParent().setRight(AVLTree.extLeaf);
			}
			y = y.getParent();

			((AVLNode) y).fixSize();
		}
		else if (y.getLeft().getHeight() != -1 && y.getRight().getHeight() == -1) {  //  if node has only left son
			y.getLeft().setParent(y.getParent());       // change node's left son parent to node's parent
			if (isLeftSon(y)) {                         // if node is left son
				y.getParent().setLeft(y.getLeft());     // change node's parent left son to node's left son
			} else {                                    // if is right son
				y.getParent().setRight(y.getLeft());    // change node's parent right son to node's left son
			}
			y = y.getParent();
			((AVLNode) y).fixSize();
		}
		else if (y.getLeft().getHeight() == -1 && y.getRight().getHeight() != -1) {  //if node has only right son
			y.getRight().setParent(y.getParent());      // change node's right son parent to node's parent
			if (isLeftSon(y)) {                         // if node is left son
				y.getParent().setLeft(y.getRight());    // change node's parent left son to node's right son

			} else {                                    // if node is right son
				y.getParent().setRight(y.getRight());   // change node's parent right son to node's right son
			}
			y = y.getParent();
			((AVLNode) y).fixSize();
		}

		if(y_key == this.min_node.getKey()){            // if y_key is the current min we need to update the min
			this.min_node = this.CalcMin(y);
		}
		if(y_key == this.max_node.getKey()){            // if y_key is the current max we need to update the max
			this.max_node = this.CalcMax(y);
		}

		// Rebalancing Process
		int steps = 0;

		steps = DeleteBalance(y);

		return steps;
	}
	/**
	 * public String min()
	 * <p>
	 * Returns the info of the item with the smallest key in the tree,
	 * or null if the tree is empty
	 */
	public String min() {
		if(this.root == AVLTree.extLeaf){
			return null;
		}
		return this.min_node.getValue();
	}
	/** function to calc the current max in the tree */
	public IAVLNode CalcMin(IAVLNode node) {
		while (node.getLeft() != AVLTree.extLeaf) { // go as much left as possible and return the min node
			node = node.getLeft();
		}
		return node;
	}
	/**
	 * public String max()
	 * <p>
	 * Returns the info of the item with the largest key in the tree,
	 * or null if the tree is empty
	 */
	public String max() {
		if(this.root == AVLTree.extLeaf){
			return null;
		}
		return this.max_node.getValue();
	}
	/** function to calc the current max in the tree */
	public IAVLNode CalcMax(IAVLNode node){
		while (node.getRight() != AVLTree.extLeaf) { // go as much right as possible and return the min node
			node = node.getRight();
		}
		return node;
	}
	/**
	 * public int[] keysToArray()
	 * <p>
	 * Returns a sorted array which contains all keys in the tree,
	 * or an empty array if the tree is empty.
	 */

	public int[] keysToArray() {
		if (this.empty()) {            // return empty array if the tree is empty
			return new int[0];
		}
		IAVLNode node = this.min_node;  // the node with minimum key value
		int[] arr = new int[this.size()];
		arr[0] = node.getKey();
		for (int i = 1; i < arr.length; i++) {  // loop over all tree nodes
			arr[i] = successor(node).getKey();
			node = successor(node);
		}
		return arr;
	}
	/**
	 * public String[] infoToArray()
	 * <p>
	 * Returns an array which contains all info in the tree,
	 * sorted by their respective keys,
	 * or an empty array if the tree is empty.
	 */

	public String[] infoToArray() {
		if (this.empty()) {
			return new String[0];
		}   //return empty array if the tree is empty
		IAVLNode node = this.min_node;
		String[] arr = new String[size()];
		arr[0] = node.getValue();
		for (int i = 1; i < arr.length; i++) {
			arr[i] = successor(node).getValue();
			node = successor(node);
		}
		return arr;
	}
	/**
	 * public int size()
	 * <p>
	 * Returns the number of nodes in the tree.
	 * <p>
	 * precondition: none
	 * postcondition: none
	 */
	public int size() {
		if(this.root.isRealNode()) {
			return ((AVLNode) this.root).getSize();
		}
		else{ return 0;}
	}
	/**
	 * public int getRoot()
	 * <p>
	 * Returns the root AVL node, or null if the tree is empty
	 * <p>
	 * precondition: none
	 * postcondition: none
	 */
	public IAVLNode getRoot() {
		if (this.empty()) {
			return null;
		}
		return this.root;
	}

	/**
	 * Function that change the root of the tree
	 * The function also update the min_node and max_node field
	 * Worst case efficiency is O(logn)
	 * precondition: newRoot is a root of a valid AVL tree
	 *
	 */

	public void setRootForTree(IAVLNode newRoot, AVLTree t){
		if(newRoot == AVLTree.extLeaf){
			t.root = AVLTree.extLeaf;
			t.min_node = null;
			t.max_node = null;
		}
		else {
			t.root = newRoot;
			t.min_node = t.CalcMin(t.root);
			t.max_node = t.CalcMax(t.root);
		}
	}
	/**
	 * public string split(int x)
	 * <p>
	 * splits the tree into 2 trees according to the key x.
	 * Returns an array [t1, t2] with two AVL trees. keys(t1) < x < keys(t2).
	 * precondition: search(x) != null (i.e. you can also assume that the tree is not empty)
	 * postcondition: none
	 */
	public AVLTree[] split(int x) {

		IAVLNode nodex = this.TreePosition(this.root, x);              // the node of x
		AVLTree t1 = new AVLTree();                                    // tree with keys() < x
		AVLTree t2 = new AVLTree();                                    // tree with keys() > x
		IAVLNode left_node = nodex.getLeft();
		IAVLNode right_node = nodex.getRight();
		// --- Insert the left subtree of nodex to t1 and the right subtree to t2
		if(left_node.getHeight() != -1){
			AVLTree tmp_tree = new AVLTree();
			t1.join(left_node, tmp_tree);
		}
		if(right_node.getHeight() != -1){
			AVLTree tmp_tree = new AVLTree();
			t2.join(right_node, tmp_tree);
		}
		// --- now continue all the way to the root and merge t1/t2 with the relevant subtree
		while(nodex != this.root) {

			if(nodex == nodex.getParent().getRight()) {      // if nodex is right son
				left_node = nodex.getParent().getLeft();
				if (left_node.getHeight() > -1) {            // if left_node isn't a leaf we need to add it to t1
					AVLTree tmp_tree = new AVLTree();
					tmp_tree.root = left_node;
					IAVLNode nodex_parent_tree = new AVLNode(nodex.getParent().getKey(), nodex.getParent().getValue());
					tmp_tree.root.getParent().setRight(AVLTree.extLeaf);
					tmp_tree.root.setParent(null);
					t1.join(nodex_parent_tree, tmp_tree);
				}
				else{										 // if left_node is a leaf we add only nodex.parent to t1
					AVLTree tmp_tree = new AVLTree();
					IAVLNode nodex_parent_tree = new AVLNode(nodex.getParent().getKey(), nodex.getParent().getValue());
					nodex_parent_tree.setLeft(AVLTree.extLeaf);
					nodex_parent_tree.setRight(AVLTree.extLeaf);
					t1.join(nodex_parent_tree, tmp_tree);
				}
			}
			else {											 // if nodex is left son

				right_node = nodex.getParent().getRight();
				if (right_node.getHeight() > -1) {			// if right_node isn't a leaf we need to add it to t2
					AVLTree tmp_tree = new AVLTree();
					tmp_tree.root = right_node;
					IAVLNode nodex_parent_tree = new AVLNode(nodex.getParent().getKey(), nodex.getParent().getValue());
					tmp_tree.root.getParent().setLeft(AVLTree.extLeaf);
					tmp_tree.root.setParent(null);
					t2.join(nodex_parent_tree, tmp_tree);
				}
				else{										// if right_node is a leaf we add only nodex.parent to t2
					AVLTree tmp_tree = new AVLTree();
					IAVLNode nodex_parent_tree = new AVLNode(nodex.getParent().getKey(), nodex.getParent().getValue());
					nodex_parent_tree.setLeft(AVLTree.extLeaf);
					nodex_parent_tree.setRight(AVLTree.extLeaf);
					t2.join(nodex_parent_tree, tmp_tree);
				}
			}
			nodex = nodex.getParent();
		}

		return new AVLTree[] {t1, t2};
	}

	/**
	 * public join(IAVLNode x, AVLTree t)
	 * <p>
	 * joins t and x with the tree.
	 * Returns the complexity of the operation (|tree.rank - t.rank| + 1).
	 * precondition: keys(x,t) < keys() or keys(x,t) > keys(). t/tree might be empty (rank = -1).
	 * postcondition: none
	 */
	public int join(IAVLNode x, AVLTree t) {

		if ((this.empty()) && (t.empty())) {                    // if both trees are empty

			this.setRootForTree(x, this);                    // set new root for the tree and calc Min/Max
			((AVLNode) this.root).fixSize();                    // set size of root to 1
			return 1;                                           // |0 - 0| + 1
		}
		if (this.empty()) {                                     // if this.tree is empty
			this.setRootForTree(t.getRoot(), this);          // change root to t.root
			this.insert(x.getKey(), x.getValue());              // insert x to the new tree
			return t.getRoot().getHeight() + 1;                 // return |0 - t.root.rank| + 1
		}
		if (t.empty()) {                                        // if t is empty
			this.insert(x.getKey(), x.getValue()); 				// insert x to the new tree
			int tmp_height = this.root.getHeight();			    // save the curr height for return value
			return tmp_height+1;                                // return |this.rank - 0| + 1
		}

		// --- CALC RETURN VALUE
		int rank_diff_res;
		if(this.root.getHeight()>=t.getRoot().getHeight()){
			rank_diff_res = this.root.getHeight()-t.getRoot().getHeight()+1;
		}
		else{ rank_diff_res = t.getRoot().getHeight()-this.getRoot().getHeight()+1;}
 		// ----

		if (this.getRoot().getHeight() == t.getRoot().getHeight()){      // if trees have the same height

			int old_height = this.getRoot().getHeight();
			if(this.root.getKey() > t.getRoot().getKey()){               // if this.keys > t.keys()
				this.root.setParent(x);
				x.setLeft(t.getRoot());
				t.getRoot().setParent(x);
				x.setRight(this.root);
			}
			else{                                                        // if this.keys < t.keys()

				x.setLeft(this.root);
				this.root.setParent(x);
				x.setRight(t.getRoot());
				t.getRoot().setParent(x);
			}
			this.setRootForTree(x, this);                             // fix root and min/max references
			((AVLNode) this.root).fixSize();                             // fix this.root
			this.root.setHeight(old_height+1);
			return rank_diff_res;
		}

		if (this.root.getKey() < t.getRoot().getKey()) {                 // if this.keys() < t.keys() & have different heights
			if (this.root.getHeight() < t.getRoot().getHeight()) {       // if this.tree is smaller than t
				IAVLNode b = t.getRoot();
				while (b.getHeight() > this.getRoot().getHeight()        // loop until b.height == k/k-1 (k=this.root.height)
						&& b.getLeft().getHeight() != -1) {

					b = b.getLeft();
				}
				if(b == t.getRoot()){                                    // if b is t.root than b has no parent - special case
					b.setParent(x);
					x.setRight(b);
					x.setLeft(this.root);
					this.root.setParent(x);
					x.setHeight(b.getHeight() + 1);            			// correct x's height
					((AVLNode) x).fixSize();
					this.setRootForTree(x, this);                    // fix root and min/max references
					return rank_diff_res;
				}
				else {													// if b is not t.root
					x.setParent(b.getParent());
					b.getParent().setLeft(x);
					b.setParent(x);
					x.setRight(b);
					x.setLeft(this.root);
					this.root.setParent(x);
					x.setHeight(b.getHeight() + 1);                     // correct x's height
					this.root = t.getRoot();
					((AVLNode) x).fixSize();
				}
			}
			else {                                                      // if this.tree is higher than t
				IAVLNode b = this.root;
				while (b.getHeight() > t.getRoot().getHeight()          // loop until b.height == k/k-1 (k=t.root.height)
						&& b.getRight().getHeight() != -1) {
					b = b.getRight();
				}
				if(b == this.root){  									// if b is this.root than b has no parent - special case
					x.setLeft(b);
					b.setParent(x);
					x.setRight(t.getRoot());
					t.getRoot().setParent(x);
					x.setHeight(b.getHeight() + 1);            			// correct x's height
					((AVLNode) x).fixSize();
					this.setRootForTree(x, this);                    // fix root and min/max references
					return rank_diff_res;

				}
				else {													// if b is not this.root
					x.setParent(b.getParent());
					b.getParent().setRight(x);
					b.setParent(x);
					x.setLeft(b);
					x.setRight(t.getRoot());
					t.getRoot().setParent(x);
					x.setHeight(b.getHeight() + 1);           			// correct x's height
					((AVLNode) x).fixSize();
				}
			}
		}
		else if (this.root.getKey() > t.getRoot().getKey()) {        	// if this.keys() > t.keys() & have different heights

			if (this.root.getHeight() < t.getRoot().getHeight()) {      // if this.tree is smaller that t

				IAVLNode b = t.getRoot();
				while (b.getHeight() > this.getRoot().getHeight()       // loop until b.height == k/k-1 (k=this.root.height)
						&& b.getRight().getHeight() != -1) {
					b = b.getRight();
				}

				if (b == t.getRoot()) {                                 // if b is t.root than b has no parent - special case
					x.setLeft(b);
					b.setParent(x);
					x.setRight(this.root);
					this.root.setParent(x);
					x.setHeight(b.getHeight() + 1);                     // correct x's height
					((AVLNode) x).fixSize();
					this.setRootForTree(x, this);                             // fix root and min/max references
					return rank_diff_res;
				} else {                                                // if b is not t.root
					x.setParent(b.getParent());
					b.getParent().setRight(x);
					b.setParent(x);
					x.setLeft(b);
					x.setRight(this.root);
					this.root.setParent(x);
					x.setHeight(b.getHeight() + 1);                     // correct x's height
					((AVLNode) x).fixSize();
					this.root = t.getRoot();
				}
			}
			else {                                                      // if this.tree is higher than t
				IAVLNode b = this.root;
				while (b.getHeight() > t.getRoot().getHeight() 			// loop until b.height == k/k-1 (k=t.root.height)
						&& b.getLeft().getHeight() != -1) {
					b = b.getLeft();
				}

				if(b == this.root) {                                    // concat 2 root with x
					x.setRight(b);
					b.setParent(x);
					x.setLeft(t.getRoot());
					t.getRoot().setParent(x);
					x.setHeight(b.getHeight() + 1);                    // correct x's height
					((AVLNode) x).fixSize();
					this.setRootForTree(x, this);                             // fix root and min/max references
					return rank_diff_res;
				}
				else {												   // if b is not this.root
					x.setParent(b.getParent());
					b.getParent().setLeft(x);
					x.setRight(b);
					b.setParent(x);
					x.setLeft(t.getRoot());
					t.getRoot().setParent(x);
					x.setHeight(b.getHeight() + 1);            			// correct x's height
					((AVLNode) x).fixSize();
				}
			}
		}

		int rank_diff_c_left = ((AVLNode) x.getParent()).rank_difference(x.getParent().getLeft());
		int rank_diff_c_right = ((AVLNode) x.getParent()).rank_difference(x.getParent().getRight());
		int rank_diff_x_left = ((AVLNode) x).rank_difference(x.getLeft());
		int rank_diff_x_right = ((AVLNode) x).rank_difference(x.getRight());
		IAVLNode curr= AVLTree.extLeaf;

		if(rank_diff_c_left == 0 && rank_diff_c_right == 2) {            // check if we are in special case of unbalancing
			if (rank_diff_x_left == 1 && rank_diff_x_right == 1) {
				curr = rotate_right(x.getParent(), x);
				curr.setHeight(curr.getHeight()+1);
				fixParentPointer(curr, x.getRight().getKey());
				((AVLNode) curr.getRight()).fixSize();
				((AVLNode) curr).fixSize();
				if(curr.getParent() == null){							// if we are in root
					this.setRootForTree(curr, this); 				// change this.root to new root and calc min/max references
					return rank_diff_res;
				}
				InsertBalance(curr.getParent());
			}
		}
		else if(rank_diff_c_left == 2 && rank_diff_c_right == 0) {       // check if we are in special case of unbalancing
			if (rank_diff_x_left == 1 && rank_diff_x_right == 1) {
				curr = rotate_left(x.getParent(), x);
				curr.setHeight(curr.getHeight()+1);
				fixParentPointer(curr, x.getLeft().getKey());
				((AVLNode) curr.getLeft()).fixSize();
				((AVLNode) curr).fixSize();
				if(curr.getParent() == null){							// if we are in root
					this.setRootForTree(curr, this); 				// change this.root to new root and calc min/max references
					return rank_diff_res;
				}
				InsertBalance(curr.getParent());
			}
		}
		else {
			curr = x.getParent();
			((AVLNode) curr).fixSize();
			InsertBalance(curr);
		}

		this.min_node = this.CalcMin(this.root);
		this.max_node = this.CalcMax(this.root);
		return rank_diff_res;
	}

	public IAVLNode successor(IAVLNode x) { // return the successor of node x
		if (x.getRight() != extLeaf) {      // if the successor is in the right subtree
			return this.CalcMin(x.getRight());
		}
		IAVLNode y = x.getParent();         // if we need to go up to find the successor
		while (y!=extLeaf && x == y.getRight()) {
			x = y;
			y = x.getParent();
		}
		return y;
	}

	/** return the position we need to insert the node in the tree
	 * if the tree is empty() return null
	*/
	public IAVLNode TreePosition(IAVLNode x, int key) {

		if (x==null) {      //if the tree is empty return null

			return null;
		}
		IAVLNode y = null;
		while (x!=AVLTree.extLeaf) {    // else search for node.key=key
			y = x;
			if (key == x.getKey()) {
				return x;               // if you found, return node
			} else if (key < x.getKey()) {
				x = x.getLeft();
			} else {
				x = x.getRight();
			}
		}
		return y; // return the place where node should be inserted
	}

	/** return true is the node is a left son */
	public static boolean isLeftSon(IAVLNode node) {
		return node.getParent().getLeft().getKey() == node.getKey();
	}

	/**
	 * public interface IAVLNode
	 * ! Do not delete or modify this - otherwise all tests will fail !
	 */
	public interface IAVLNode {
		public int getKey(); //returns node's key (for virtuval node return -1)

		public String getValue(); //returns node's value [info] (for virtuval node return null)

		public void setLeft(IAVLNode node); //sets left child

		public IAVLNode getLeft(); //returns left child (if there is no left child return null)

		public void setRight(IAVLNode node); //sets right child

		public IAVLNode getRight(); //returns right child (if there is no right child return null)

		public void setParent(IAVLNode node); //sets parent

		public IAVLNode getParent(); //returns the parent (if there is no parent return null)

		public boolean isRealNode(); // Returns True if this is a non-virtual AVL node

		public void setHeight(int height); // sets the height of the node

		public int getHeight(); // Returns the height of the node (-1 for virtual nodes)

	}
	public static class ExtLeaf implements IAVLNode{
		private int key;
		private String info;
		private int size = 0;
		private final int height=-1;
		private IAVLNode left = null;
		private IAVLNode right = null;
		private IAVLNode parent = null;

		public int getKey(){return -1;}
		public String getValue(){return null;}
		public void setLeft(IAVLNode node){}

		public IAVLNode getLeft(){return null;}
		public void setRight(IAVLNode node){}
		public IAVLNode getRight(){return null;}
		public void setParent(IAVLNode node){}
		public IAVLNode getParent(){return null;}
		public boolean isRealNode(){return false;}
		public void setHeight(int height){}
		public int getHeight(){return this.height;}
	}
	/**
	 * public class AVLNode
	 * <p>
	 * If you wish to implement classes other than AVLTree
	 * (for example AVLNode), do it in this file, not in
	 * another file.
	 * This class can and must be modified.
	 * (It must implement IAVLNode)
	 */

	public class AVLNode implements IAVLNode {

		private int key;
		private String info;
		private int height;
		private int size;
		private IAVLNode left = null;
		private IAVLNode right = null;
		private IAVLNode parent = null;

		public AVLNode(int key, String value) {
			this.key = key;
			this.info = value;
		}

		public int getKey() {
			return key;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public String getValue() {
			return info;
		}

		public void setValue(String value) {
			this.info = value;
		}

		public void setLeft(IAVLNode node) {
			this.left = node;
		}

		public IAVLNode getLeft() {
			return left;
		}

		public void setRight(IAVLNode node) {
			this.right = node;
		}

		public IAVLNode getRight() {
			return right;
		}

		public void setParent(IAVLNode node) {
			this.parent = node;
		}

		public IAVLNode getParent() {
			return parent;
		}

		// Returns True if this is a non-virtual AVL node
		public boolean isRealNode() {
			return true;
		}

		public void setHeight(int height) {
			this.height = height;
		}

		public int getHeight() {
			return height;
		}

		/**
		 * public int rank_difference(IAVLNode p, IAVLNode s)
		 * <p>
		 * return the rank difference between 2 nodes.
		 */
		public int rank_difference(IAVLNode son) {
			return this.getHeight() - son.getHeight();
		}
		public int getSize(){return this.size;}
		public void setSize(int size){this.size = size;}

		public void fixSize() {
			if (this.getLeft().getHeight() == -1 && this.getRight().getHeight() == -1) { // the node is a leaf size = 1
				this.size = 1;
			}
			else {               // if the node isn't a leaf, we need to update his size based on his sons.
				int left_size=0;
				int right_size=0;
				if (this.getLeft().isRealNode()) {
					left_size = ((AVLNode) this.getLeft()).getSize();
				}
				if (this.getRight().isRealNode()) {
					right_size = ((AVLNode) this.getRight()).getSize();
				}

				this.size = left_size + right_size + 1;
			}
		}
	}
}

